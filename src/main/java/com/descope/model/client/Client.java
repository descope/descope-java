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
  // When set, FGA calls that the FGA cache serves go here instead of uri.
  private String fgaCacheUri;
  // When set, sent along with every authentication request so that authentication methods whose
  // public access has been disabled can still be used. Never sent on management requests.
  private String authManagementKey;

  // Keeps the pre-fgaCacheUri all-args constructor available to callers that use it positionally.
  public Client(String uri, String projectId, String managementKey, Map<String, String> headers,
      SdkInfo sdkInfo, Key providedKey, AtomicReference<Map<String, Key>> keys) {
    this(uri, projectId, managementKey, headers, sdkInfo, providedKey, keys, null, null);
  }

  // Keeps the pre-authManagementKey all-args constructor available to callers that use it
  // positionally.
  public Client(String uri, String projectId, String managementKey, Map<String, String> headers,
      SdkInfo sdkInfo, Key providedKey, AtomicReference<Map<String, Key>> keys, String fgaCacheUri) {
    this(uri, projectId, managementKey, headers, sdkInfo, providedKey, keys, fgaCacheUri, null);
  }

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
