package com.descope.utils;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * A few utility methods since we are using Java8.
 */
@UtilityClass
public class CollectionUtils {
  public static <K, V> Map<K, V> mapOf(K k, V v) {
    Map<K, V> m = new HashMap<>();
    m.put(k, v);
    return m;
  }

  public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    return m;
  }

  public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    return m;
  }

  public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    return m;
  }

  public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    m.put(k5, v5);
    return m;
  }

  public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    m.put(k5, v5);
    m.put(k6, v6);
    return m;
  }
}
