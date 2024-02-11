package com.descope.utils;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class UriUtils {
  public static URI addPath(URI uri, String path) {
    String newPath;
    if (path.startsWith("/")) {
      newPath = path.replaceAll("//+", "/");
    } else if (uri.getPath().endsWith("/")) {
      newPath = uri.getPath() + path.replaceAll("//+", "/");
    } else {
      newPath = uri.getPath() + "/" + path.replaceAll("//+", "/");
    }
    return uri.resolve(newPath).normalize();
  }

  public URI getUri(String url, String path) {
    return URI.create(url + (path.startsWith("/") ? path : "/" + path));
  }

  @SneakyThrows
  public static Map<String, List<String>> splitQuery(String url) {
    if (StringUtils.isBlank(url)) {
      return Collections.emptyMap();
    }
    URL newUrl = new URL(url);
    return splitQuery(newUrl);
  }

  public static Map<String, List<String>> splitQuery(URL url) {
    if (StringUtils.isBlank(url.getQuery())) {
      return Collections.emptyMap();
    }

    return Arrays.stream(url.getQuery().split("&")).map(UriUtils::splitQueryParameter)
        .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }

  @SneakyThrows
  public static SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
    final int idx = it.indexOf("=");
    final String key = idx > 0 ? it.substring(0, idx) : it;
    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
    return new SimpleImmutableEntry<>(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
  }
}
