package com.descope.proxy.impl;

import com.descope.proxy.ApiProxy;
import java.net.URI;
import java.util.function.Supplier;

class ApiProxyImpl extends AbstractProxyImpl implements ApiProxy {

  public ApiProxyImpl() {}

  public ApiProxyImpl(Supplier<String> authHeaderSupplier) {
    setAuthHeader("Authorization", authHeaderSupplier);
  }

  @Override
  public <B, R> R post(URI uri, B body, Class<R> returnClz) {
    return super.post(uri, body, returnClz);
  }

  @Override
  public <R> R get(URI uri, Class<R> returnClz) {
    return super.get(uri, returnClz);
  }
}
