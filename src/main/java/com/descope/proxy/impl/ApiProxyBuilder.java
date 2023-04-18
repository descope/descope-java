package com.descope.proxy.impl;

import com.descope.proxy.ApiProxy;
import java.util.function.Supplier;

public class ApiProxyBuilder {

  public static ApiProxy buildProxy() {
    return new ApiProxyImpl();
  }

  public static ApiProxy buildProxy(Supplier<String> authHeaderSupplier){
    return new ApiProxyImpl(authHeaderSupplier);
  }

}
