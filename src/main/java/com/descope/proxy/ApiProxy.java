package com.descope.proxy;

import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;

public interface ApiProxy {
  <R> R get(URI uri, Class<R> returnClz);

  <R> R getArray(URI uri, TypeReference<R> typeReference);

  <B, R> R post(URI uri, B body, Class<R> returnClz);

  <B, R> R postAndGetArray(URI uri, B body, TypeReference<R> typeReference);

  <B, R> R delete(URI uri, B body, Class<R> returnClz);
}
