package com.descope.proxy;

import java.net.URI;

public interface ApiProxy {
  <R> R get(URI uri, Class<R> returnClz);

  <B, R> R post(URI uri, B body, Class<R> returnClz);

  <B, R> R delete(URI uri, B body, Class<R> returnClz);
}
