package com.descope.proxy.impl;

import com.descope.exception.ErrorCode;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.SdkInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
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
  private static final String RETRY_AFTER_HEADER = "Retry-After";
  private static final long DEFAULT_RETRY = 60;

  private String authHeaderKey;
  private Supplier<String> authHeaderSupplier; // supplies value of AUTHORIZATION header
  private SdkInfo sdkInfo;

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
      return httpClient.execute(req, new HttpClientResponseHandler<R>() {
        @SuppressWarnings("resource")
        @Override
        public R handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
          try (final ClassicHttpResponse res = response) {
            final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            final TeeInputStream tee = new TeeInputStream(res.getEntity().getContent(), bs, true);

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
              } catch (IOException e) {
                throw ServerCommonException.genericServerError(
                  bs.toString(), String.valueOf(res.getCode()), bs.toString());
              }
            }
            try {
              R r = returnClz != null
                  ? objectMapper.readValue(tee, returnClz)
                  : objectMapper.readValue(tee, typeReference);
              if (log.isDebugEnabled()) {
                String resStr = bs.toString();
                log.debug(String.format("Received response %s",
                    resStr.substring(0, resStr.length() > 10000 ? 10000 : resStr.length())));
              }
              return r;
            } catch (Exception e) {
              throw ServerCommonException.parseResponseError("Error parsing response", bs.toString(), e);
            }
          }
        }
      });
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
}
