package com.descope.sdk;

import com.descope.model.client.Client;
import com.descope.utils.UriUtils;

import java.net.URI;
import java.util.Map;

import static com.descope.utils.UriUtils.addPath;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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

  protected URI getQueryParamUri(String path, Map<String, String> params) {
    if (isNotEmpty(params)) {
      String queryParams = params.entrySet().stream()
          .map(p -> p.getKey() + "=" + p.getValue())
          .reduce((p1, p2) -> p1 + "&" + p2)
          .orElse("");
      return UriUtils.getUri(client.getUri(), path.concat("?" + queryParams));
    }
    return UriUtils.getUri(client.getUri(), path);
  }
}
