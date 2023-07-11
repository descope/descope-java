package com.descope.proxy.impl;

import com.descope.exception.ServerCommonException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.commons.lang3.StringUtils;

@Slf4j
abstract class AbstractProxyImpl {

  private String authHeaderKey;
  private Supplier<String> authHeaderSupplier; // supplies value of AUTHORIZATION header

  @SneakyThrows
  private static <B> BodyPublisher getBodyPublisher(B body) {
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

    private static <R> Supplier<R> toSupplierOfType(
        InputStream inputStream, HttpResponse.ResponseInfo responseInfo, Class<R> returnClz) {
      return () -> {
        try (InputStream stream = inputStream) {
          ObjectMapper objectMapper = new ObjectMapper();
          if (responseInfo.statusCode() < 200 || responseInfo.statusCode() > 299) {
            var errorDetails = objectMapper.readValue(stream, JsonBodyHandler.ErrorDetails.class);
            log.error(errorDetails.getActualMessage());
            throw ServerCommonException.genericServerError(
                errorDetails.getActualMessage(), errorDetails.getErrorCode());
          }
          return objectMapper.readValue(stream, returnClz);
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
