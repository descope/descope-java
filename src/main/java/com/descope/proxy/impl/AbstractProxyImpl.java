package com.descope.proxy.impl;

import com.descope.exception.ErrorCode;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.SdkInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;

@Slf4j
abstract class AbstractProxyImpl {

  private String authHeaderKey;
  private Supplier<String> authHeaderSupplier; // supplies value of AUTHORIZATION header
  private SdkInfo sdkInfo;

  @SneakyThrows
  private static <B> BodyPublisher getBodyPublisher(B body) {
    if (body == null) {
      return BodyPublishers.ofString("");
    }
    var objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
    return BodyPublishers.ofString(requestBody);
  }

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

  @SneakyThrows
  <B, R> R exchange(URI uri, String method, B body, Class<R> returnClz) {
    Builder httpRequestBuilder = HttpRequest.newBuilder().uri(uri);
    addHeaders(httpRequestBuilder);

    var httpRequest =
        "GET".equals(method)
            ? httpRequestBuilder.GET().build()
            : httpRequestBuilder.method(method, getBodyPublisher(body)).build();

    var httpClient = HttpClient.newHttpClient();
    JsonBodyHandler<R> bodyHandler = new JsonBodyHandler<>(returnClz);
    log.debug(String.format("Sending %s request to %s", method, uri.toString()));
    Supplier<R> responseSupplier = httpClient.send(httpRequest, bodyHandler).body();
    return responseSupplier.get();
  }

  private void addHeaders(Builder httpRequestBuilder) {
    httpRequestBuilder.header("Content-Type", "application/json");
    if (StringUtils.isNotBlank(authHeaderKey)) {
      String authHeaderVal = authHeaderSupplier.get();
      httpRequestBuilder.header(authHeaderKey, authHeaderVal);
    }
    if (sdkInfo != null) {
      if (StringUtils.isNotBlank(sdkInfo.getJavaVersion())) {
        httpRequestBuilder.header("x-descope-sdk-java-version", sdkInfo.getJavaVersion());
      }
      if (StringUtils.isNotBlank(sdkInfo.getName())) {
        httpRequestBuilder.header("x-descope-sdk-name", sdkInfo.getName());
      }
      if (StringUtils.isNotBlank(sdkInfo.getVersion())) {
        httpRequestBuilder.header("x-descope-sdk-version", sdkInfo.getVersion());
      }
      if (StringUtils.isNotBlank(sdkInfo.getSha())) {
        httpRequestBuilder.header("x-descope-sdk-sha", sdkInfo.getSha());
      }
    }
  }

  protected <B, R> R post(URI uri, B body, Class<R> returnClz) {
    return exchange(uri, "POST", body, returnClz);
  }

  protected <R> R get(URI uri, Class<R> returnClz) {
    return exchange(uri, "GET", null, returnClz);
  }

  protected <B, R> R delete(URI uri, B body, Class<R> returnClz) {
    return exchange(uri, "DELETE", body, returnClz);
  }

  private static class JsonBodyHandler<R> implements HttpResponse.BodyHandler<Supplier<R>> {

    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final long DEFAULT_RETRY = 60;

    private final Class<R> returnClz;

    public JsonBodyHandler(Class<R> returnClz) {
      this.returnClz = returnClz;
    }

    private static <R> HttpResponse.BodySubscriber<Supplier<R>> asJson(
        HttpResponse.ResponseInfo responseInfo, Class<R> returnClz) {
      HttpResponse.BodySubscriber<InputStream> upstream =
          HttpResponse.BodySubscribers.ofInputStream();

      return HttpResponse.BodySubscribers.mapping(
          upstream, inputStream -> toSupplierOfType(inputStream, responseInfo, returnClz));
    }

    @SuppressWarnings({"resource"})
    private static <R> Supplier<R> toSupplierOfType(
        InputStream inputStream, HttpResponse.ResponseInfo responseInfo, Class<R> returnClz) {
      return () -> {
        try (InputStream stream = inputStream) {
          ObjectMapper objectMapper = new ObjectMapper();
          ByteArrayOutputStream bs = new ByteArrayOutputStream();
          TeeInputStream tee = new TeeInputStream(stream, bs, true);
          if (responseInfo.statusCode() < 200 || responseInfo.statusCode() > 299) {
            try {
              var errorDetails = objectMapper.readValue(tee, JsonBodyHandler.ErrorDetails.class);
              log.error(errorDetails.getActualMessage());
              if (ErrorCode.RATE_LIMIT_EXCEEDED.equals(errorDetails.errorCode)) {
                throw new RateLimitExceededException(
                  errorDetails.getActualMessage(),
                  errorDetails.getErrorCode(),
                  responseInfo.headers().firstValueAsLong(RETRY_AFTER_HEADER).orElse(DEFAULT_RETRY));
              }
              throw ServerCommonException.genericServerError(
                  errorDetails.getActualMessage(), errorDetails.getErrorCode());
            } catch (IOException e) {
              throw ServerCommonException.genericServerError(
                  bs.toString(), String.valueOf(responseInfo.statusCode()));
            }
          }
          var res = objectMapper.readValue(tee, returnClz);
          if (log.isDebugEnabled()) {
            String resStr = bs.toString();
            log.debug(String.format("Received response %s",
                resStr.substring(0, resStr.length() > 10000 ? 10000 : resStr.length())));
          }
          return res;
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      };
    }

    @Override
    public HttpResponse.BodySubscriber<Supplier<R>> apply(HttpResponse.ResponseInfo responseInfo) {
      return asJson(responseInfo, returnClz);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ErrorDetails {
      private String errorCode;
      private String errorDescription;
      private String errorMessage;
      private String message;

      String getActualMessage() {
        return errorMessage == null ? message : errorMessage;
      }
    }
  }
}
