package com.descope.proxy.impl;

import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.function.Supplier;

class ApiProxyImpl extends AbstractProxyImpl implements ApiProxy {

  public ApiProxyImpl(SdkInfo sdkInfo) {
    setSdkInfo(sdkInfo);
  }

  public ApiProxyImpl(Supplier<String> authHeaderSupplier, SdkInfo sdkInfo) {
    setAuthHeader("Authorization", authHeaderSupplier);
    setSdkInfo(sdkInfo);
  }

  @Override
  public <B, R> R post(URI uri, B body, Class<R> returnClz) {
    return super.post(uri, body, returnClz);
  }

  @Override
  public <B, R> R postAndGetArray(URI uri, B body, TypeReference<R> typeReference) {
    return super.post(uri, body, typeReference);
  }

  @Override
  public <R> R get(URI uri, Class<R> returnClz) {
    return super.get(uri, returnClz);
  }

  @Override
  public <R> R getArray(URI uri, TypeReference<R> typeReference) {
    return super.get(uri, typeReference);
  }

  @Override
  public <B, R> R delete(URI uri, B body, Class<R> returnClz) {
    return super.delete(uri, body, returnClz);
  }
}
