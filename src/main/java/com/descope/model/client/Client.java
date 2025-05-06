package com.descope.model.client;

import com.descope.sdk.auth.impl.KeyProvider;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
  private String uri;
  private String projectId;
  private String managementKey;
  private Map<String, String> headers;
  private SdkInfo sdkInfo;
  private Key providedKey;
  @Builder.Default
  private AtomicReference<Map<String, Key>> keys = new AtomicReference<>(new HashMap<>());

  public Key getKey(String keyId) {
    if (providedKey != null) {
      return providedKey;
    }
    Key k = keys.get().get(keyId);
    // If key is not found, try to refresh key cache
    if (k == null) {
      keys.set(KeyProvider.getKeys(projectId, uri, this));
      k = keys.get().get(keyId);
    }
    return k;
  }
}
