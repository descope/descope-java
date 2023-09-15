package com.descope.sdk;

import static com.descope.utils.UriUtils.addPath;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.descope.exception.ClientFunctionalException;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.sdk.auth.impl.KeyProvider;
import com.descope.utils.JwtUtils;
import com.descope.utils.UriUtils;
import java.net.URI;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public abstract class SdkServicesBase {
  protected final Client client;

  protected SdkServicesBase(Client client, String projectId) {
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
      for (var e : params.entrySet()) {
        if (sb.length() > 0) {
          sb.append('&');
        }
        sb.append(
            URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(
            URLEncoder.encode(e.getValue(), "UTF-8"));
      }
      return UriUtils.getUri(client.getUri(), path.concat(sb.toString()));
    }
    return UriUtils.getUri(client.getUri(), path);
  }

  @SneakyThrows
  protected Key requestKeys() {
    var key = client.getProvidedKey();
    if (key != null) {
      return client.getProvidedKey();
    }

    key = KeyProvider.getKey(client.getParams().getProjectId(), client.getUri(), client.getSdkInfo());
    client.setProvidedKey(key);
    return key;
  }

  protected Token validateAndCreateToken(String jwt) {
    if (StringUtils.isBlank(jwt)) {
      throw ClientFunctionalException.invalidToken();
    }
    return JwtUtils.getToken(jwt, requestKeys());
  }

}
