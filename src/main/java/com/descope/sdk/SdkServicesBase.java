package com.descope.sdk;

import static com.descope.utils.UriUtils.addPath;

import com.descope.model.client.Client;
import com.descope.utils.UriUtils;
import java.net.URI;

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
}
