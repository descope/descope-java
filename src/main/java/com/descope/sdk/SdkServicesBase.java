package com.descope.sdk;

import static com.descope.utils.UriUtils.addPath;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.descope.exception.ClientFunctionalException;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.utils.JwtUtils;
import com.descope.utils.UriUtils;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public abstract class SdkServicesBase {
  protected final Client client;

  protected SdkServicesBase(Client client) {
    this.client = client;
  }

  protected URI composeURI(String base, String path) {
    URI uri = getUri(base);
    return addPath(uri, path);
  }

  protected URI getUri(String path) {
    return UriUtils.getUri(client.getUri(), path);
  }

  @SneakyThrows
  protected URI getQueryParamUri(String path, Map<String, String> params) {
    if (isNotEmpty(params)) {
      StringBuilder sb = new StringBuilder("?");
      for (Entry<String, String> e : params.entrySet()) {
        if (sb.length() > 1) {
          sb.append('&');
        }
        sb.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
      }
      return UriUtils.getUri(client.getUri(), path.concat(sb.toString()));
    }
    return UriUtils.getUri(client.getUri(), path);
  }

  protected Token validateAndCreateToken(String jwt) {
    if (StringUtils.isBlank(jwt)) {
      throw ClientFunctionalException.invalidToken();
    }
    return JwtUtils.getToken(jwt, client);
  }

  @SneakyThrows
  protected String appendQueryParams(String url, Map<String, String> params) {
    if (isNotEmpty(params)) {
      URI oldUri = new URI(url);
      
      StringBuilder sb = new StringBuilder();
      // Add existing query parameters (get the raw encoded query from the original URL)
      String existingQuery = oldUri.getRawQuery(); // Use getRawQuery() to keep original encoding
      if (existingQuery != null) {
        sb.append(existingQuery);
      }

      // Add new parameters (encode them)
      for (Entry<String, String> e : params.entrySet()) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
      }
      
      // Build URL manually to avoid double encoding
      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append(oldUri.getScheme()).append("://");
      if (oldUri.getAuthority() != null) {
        urlBuilder.append(oldUri.getRawAuthority());
      }
      if (oldUri.getPath() != null) {
        urlBuilder.append(oldUri.getRawPath());
      }
      if (sb.length() > 0) {
        urlBuilder.append("?").append(sb.toString());
      }
      if (oldUri.getFragment() != null) {
        urlBuilder.append("#").append(oldUri.getRawFragment());
      }
      return urlBuilder.toString();
    }
    return url;
  }
}
