package com.descope.utils;

import java.net.URI;
import lombok.experimental.UtilityClass;

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
}
