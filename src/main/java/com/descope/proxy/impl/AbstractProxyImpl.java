package com.descope.proxy.impl;

import com.descope.exception.ErrorCode;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

@Slf4j
abstract class AbstractProxyImpl {

  // HTTP status codes that should trigger automatic retries:
  // 503: Service Unavailable
  // 521: Web Server Is Down (Cloudflare)
  // 522: Connection Timed Out (Cloudflare)
  // 524: A Timeout Occurred (Cloudflare)
  // 530: Cloudflare error
  static final Set<Integer> retryableStatusCodes = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList(503, 521, 522, 524, 530)));

  // Retry delays in milliseconds: first retry after 100ms, subsequent retries after 5000ms.
  // Package-private to allow overriding in tests.
  static long[] retryDelaysMs = {100L, 5000L, 5000L};

  private static final String RETRY_AFTER_HEADER = "Retry-After";

  private static final long DEFAULT_RETRY = 60;

  private String authHeaderKey;

  private Supplier<String> authHeaderSupplier; // supplies value of AUTHORIZATION header

  private SdkInfo sdkInfo;

  protected Client client;

  protected void setAuthHeader(String authHeaderKey, Supplier<String> authHeaderSupplier) {
    if (StringUtils.isNotBlank(authHeaderKey) && authHeaderSupplier != null) {
      this.authHeaderKey = authHeaderKey;
      this.authHeaderSupplier = authHeaderSupplier;
    } else if (StringUtils.isBlank(authHeaderKey)) {
      this.authHeaderKey = null;
      this.authHeaderSupplier = null;
    }
  }

  protected void setSdkInfo(SdkInfo sdkInfo) {
    this.sdkInfo = sdkInfo;
  }

  long getRetryHeader(final ClassicHttpResponse res) {
    long retryPeriod = DEFAULT_RETRY;
    if (res.containsHeader(RETRY_AFTER_HEADER)) {
      try {
        retryPeriod = Long.parseLong(res.getFirstHeader(RETRY_AFTER_HEADER).getValue());
      } catch (NumberFormatException nfe) {
        // Ignore
      }
    }
    return retryPeriod;
  }

  @SneakyThrows
  <B, R> R exchange(ClassicHttpRequest req, Class<R> returnClz, TypeReference<R> typeReference) {
    addHeaders(req);
    log.debug(String.format("Sending %s request to %s", req.getMethod(), req.getRequestUri()));
    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      for (int retryIdx = 0; ; retryIdx++) {
        final int currentRetryIdx = retryIdx;
        try {
          return httpClient.execute(req, new HttpClientResponseHandler<R>() {
            @SuppressWarnings("resource")
            @Override
            public R handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
              try (final ClassicHttpResponse res = response) {
                if (retryableStatusCodes.contains(res.getCode())
                    && currentRetryIdx < retryDelaysMs.length) {
                  throw new RetryableStatusException(res.getCode());
                }
                final ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                final ByteArrayOutputStream bs = new ByteArrayOutputStream();
                if (res.getEntity() == null) {
                  throw ServerCommonException.genericServerError(
                    "Empty response", String.valueOf(res.getCode()), "");
                }
                final TeeInputStream tee =
                    new TeeInputStream(res.getEntity().getContent(), bs, true);

                if (res.getCode() < 200 || response.getCode() > 299) {
                  if (res.getCode() == 429) { // Rate limit from infra
                    throw new RateLimitExceededException(
                      "Rate limit exceeded",
                      ErrorCode.RATE_LIMIT_EXCEEDED,
                      getRetryHeader(res));
                  }
                  try {
                    ErrorDetails errorDetails = objectMapper.readValue(tee, ErrorDetails.class);
                    log.debug(errorDetails.getActualMessage());
                    log.debug(bs.toString());
                    if (ErrorCode.RATE_LIMIT_EXCEEDED.equals(errorDetails.getErrorCode())) {
                      throw new RateLimitExceededException(
                        errorDetails.getActualMessage(),
                        errorDetails.getErrorCode(),
                        getRetryHeader(res));
                    }
                    throw ServerCommonException.genericServerError(
                      errorDetails.getActualMessage(),
                      StringUtils.isBlank(errorDetails.getErrorCode())
                        ? String.valueOf(res.getCode())
                        : errorDetails.getErrorCode(),
                      bs.toString());
                  } catch (IOException ex) {
                    throw ServerCommonException.genericServerError(
                      bs.toString(), String.valueOf(res.getCode()), bs.toString());
                  }
                }
                try {
                  R rr = returnClz != null
                      ? objectMapper.readValue(tee, returnClz)
                      : objectMapper.readValue(tee, typeReference);
                  if (log.isDebugEnabled()) {
                    String resStr = bs.toString();
                    log.debug(String.format("Received response %s",
                        resStr.substring(0, resStr.length() > 10000 ? 10000 : resStr.length())));
                  }
                  return rr;
                } catch (Exception ex) {
                  throw ServerCommonException.parseResponseError(
                      "Error parsing response", bs.toString(), ex);
                }
              }
            }
          });
        } catch (RetryableStatusException ex) {
          log.info("Retrying request to {} after receiving status {}",
              req.getRequestUri(), ex.getStatusCode());
          try {
            Thread.sleep(retryDelaysMs[retryIdx]);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Request retry interrupted", ie);
          }
        }
      }
    }
  }

  private void addHeaders(ClassicHttpRequest req) {
    req.addHeader("Content-Type", "application/json");
    if (StringUtils.isNotBlank(authHeaderKey)) {
      String authHeaderVal = authHeaderSupplier.get();
      req.addHeader(authHeaderKey, authHeaderVal);
    }
    if (sdkInfo != null) {
      if (StringUtils.isNotBlank(sdkInfo.getJavaVersion())) {
        req.addHeader("x-descope-sdk-java-version", sdkInfo.getJavaVersion());
      }
      if (StringUtils.isNotBlank(sdkInfo.getName())) {
        req.addHeader("x-descope-sdk-name", sdkInfo.getName());
      }
      if (StringUtils.isNotBlank(sdkInfo.getVersion())) {
        req.addHeader("x-descope-sdk-version", sdkInfo.getVersion());
      }
      if (StringUtils.isNotBlank(sdkInfo.getSha())) {
        req.addHeader("x-descope-sdk-sha", sdkInfo.getSha());
      }
    }
    if (client != null && StringUtils.isNotBlank(client.getProjectId())) {
      req.addHeader("x-descope-project-id", client.getProjectId());
    }
  }

  @SneakyThrows
  protected <B, R> R post(URI uri, B body, Class<R> returnClz) {
    final ClassicRequestBuilder builder = ClassicRequestBuilder.post(uri);
    if (body != null) {
      final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
      final byte[] payload = objectMapper.writeValueAsBytes(body);
      builder.setEntity(new ByteArrayEntity(payload, ContentType.APPLICATION_JSON));
    }
    return exchange(builder.build(), returnClz, null);
  }

  @SneakyThrows
  protected <B, R> R post(URI uri, B body, TypeReference<R> typeReference) {
    final ClassicRequestBuilder builder = ClassicRequestBuilder.post(uri);
    if (body != null) {
      final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
      final byte[] payload = objectMapper.writeValueAsBytes(body);
      builder.setEntity(new ByteArrayEntity(payload, ContentType.APPLICATION_JSON));
    }
    return exchange(builder.build(), null, typeReference);
  }

  @SneakyThrows
  protected <B, R> R patch(URI uri, B body, Class<R> returnClz) {
    final ClassicRequestBuilder builder = ClassicRequestBuilder.patch(uri);
    if (body != null) {
      final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
      final byte[] payload = objectMapper.writeValueAsBytes(body);
      builder.setEntity(new ByteArrayEntity(payload, ContentType.APPLICATION_JSON));
    }
    return exchange(builder.build(), returnClz, null);
  }

  protected <R> R get(URI uri, Class<R> returnClz) {
    return exchange(ClassicRequestBuilder.get(uri).build(), returnClz, null);
  }

  protected <R> R get(URI uri, TypeReference<R> typeReference) {
    return exchange(ClassicRequestBuilder.get(uri).build(), null, typeReference);
  }

  protected <B, R> R delete(URI uri, B body, Class<R> returnClz) {
    return exchange(ClassicRequestBuilder.delete(uri).build(), returnClz, null);
  }

  private static class RetryableStatusException extends IOException {

    private final int statusCode;

    RetryableStatusException(int statusCode) {
      super("Retryable status code: " + statusCode);
      this.statusCode = statusCode;
    }

    int getStatusCode() {
      return statusCode;
    }
  }
}
