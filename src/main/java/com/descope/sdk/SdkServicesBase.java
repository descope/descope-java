package com.descope.sdk;

import static com.descope.utils.UriUtils.addPath;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.descope.exception.ClientFunctionalException;
import com.descope.model.client.Client;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.Token;
import com.descope.sdk.auth.impl.KeyProvider;
import com.descope.utils.JwtUtils;
import com.descope.utils.UriUtils;
import java.net.URI;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public abstract class SdkServicesBase {
  protected final Client client;
  protected final Provider provider;

  protected SdkServicesBase(Client client, String projectId) {
    this.client = client;
    this.provider =
        Provider.builder().client(client).projectId(projectId).keyMap(new HashMap<>()).build();
  }

  protected URI composeURI(String base, String path) {
    URI uri = getUri(base);
    return addPath(uri, path);
  }

  protected URI getUri(String path) {
    return UriUtils.getUri(client.getUri(), path);
  }

  protected URI getQueryParamUri(String path, Map<String, String> params) {
    if (isNotEmpty(params)) {
      String queryParams =
          params.entrySet().stream()
              .map(p -> p.getKey() + "=" + p.getValue())
              .reduce((p1, p2) -> p1 + "&" + p2)
              .orElse("");
      return UriUtils.getUri(client.getUri(), path.concat("?" + queryParams));
    }
    return UriUtils.getUri(client.getUri(), path);
  }

  @SneakyThrows
  protected Key requestKeys() {
    if (provider.getProvidedKey() != null) {
      return provider.getProvidedKey();
    }

    var key = KeyProvider.getKey(provider.getProjectId(), client.getUri(), client.getSdkInfo());
    provider.setProvidedKey(key);
    return key;
  }

  protected Token validateAndCreateToken(String jwt) {
    if (StringUtils.isBlank(jwt)) {
      throw ClientFunctionalException.invalidToken();
    }
    return JwtUtils.getToken(jwt, requestKeys());
  }

}
