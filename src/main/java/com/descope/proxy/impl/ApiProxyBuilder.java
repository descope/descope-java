package com.descope.proxy.impl;

import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiProxyBuilder {

  public static ApiProxy buildProxy(SdkInfo sdkInfo) {
    return new ApiProxyImpl(sdkInfo);
  }

  public static ApiProxy buildProxy(Supplier<String> authHeaderSupplier, SdkInfo sdkInfo) {
    return new ApiProxyImpl(authHeaderSupplier, sdkInfo);
  }
}
