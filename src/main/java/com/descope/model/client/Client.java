package com.descope.model.client;

import com.descope.sdk.auth.impl.KeyProvider;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
  // Cached signing keys are refetched on lookup once this long has passed since the last successful fetch.
  private static final long KEYS_TTL_NANOS = TimeUnit.MINUTES.toNanos(10);
  // Unknown key ids refetch at most this often after a successful fetch; stale cached keys after any attempt.
  private static final long KEYS_REFETCH_COOLDOWN_NANOS = TimeUnit.SECONDS.toNanos(30);
  // Overridden in tests.
  static LongSupplier nanoClock = System::nanoTime;

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
  // Fetched once when the management services are built and sent on every management request so
  // the edge can apply the right rate limit bucket. Blank when the handshake failed.
  private String rateLimitTier;

  // Keeps the pre-fgaCacheUri all-args constructor available to callers that use it positionally.
  public Client(String uri, String projectId, String managementKey, Map<String, String> headers,
      SdkInfo sdkInfo, Key providedKey, AtomicReference<Map<String, Key>> keys) {
    this(uri, projectId, managementKey, headers, sdkInfo, providedKey, keys, null, null, null);
  }

  // Keeps the pre-authManagementKey all-args constructor available to callers that use it
  // positionally.
  public Client(String uri, String projectId, String managementKey, Map<String, String> headers,
      SdkInfo sdkInfo, Key providedKey, AtomicReference<Map<String, Key>> keys, String fgaCacheUri) {
    this(uri, projectId, managementKey, headers, sdkInfo, providedKey, keys, fgaCacheUri, null, null);
  }

  // Keeps the pre-rateLimitTier all-args constructor available to callers that use it positionally.
  public Client(String uri, String projectId, String managementKey, Map<String, String> headers,
      SdkInfo sdkInfo, Key providedKey, AtomicReference<Map<String, Key>> keys, String fgaCacheUri,
      String authManagementKey) {
    this(uri, projectId, managementKey, headers, sdkInfo, providedKey, keys, fgaCacheUri,
        authManagementKey, null);
  }

  public Key getKey(String keyId) {
    if (providedKey != null) {
      return providedKey;
    }
    AtomicReference<Map<String, Key>> ref = keys;
    Map<String, Key> seen = ref.get();
    Key k = seen.get(keyId);
    if (k != null && (!isStale(seen, nanoClock.getAsLong()) || seen instanceof KeySet && ((KeySet) seen).refreshing)) {
      return k;
    }
    // One fetch at a time per key cache; callers that waited share its result.
    synchronized (ref) {
      Map<String, Key> current = ref.get();
      if (current != seen) {
        return current.get(keyId);
      }
      long now = nanoClock.getAsLong();
      if (current instanceof KeySet) {
        KeySet set = (KeySet) current;
        if (now - (k == null ? set.fetchedAt : set.attemptedAt) < KEYS_REFETCH_COOLDOWN_NANOS) {
          return k;
        }
        // Lets stale cached keys be served without waiting on this fetch.
        set.refreshing = true;
      }
      try {
        ref.set(new KeySet(KeyProvider.getKeys(projectId, uri, this), now, now));
      } catch (Exception e) {
        // Keep the last keys, still stale, and record the attempt so stale cached keys skip refetching for a while.
        long fetchedAt = current instanceof KeySet ? ((KeySet) current).fetchedAt : now - KEYS_TTL_NANOS;
        ref.set(new KeySet(current, fetchedAt, now));
        if (k == null) {
          throw e;
        }
        return k;
      }
      return ref.get().get(keyId);
    }
  }

  private static boolean isStale(Map<String, Key> keys, long now) {
    return !(keys instanceof KeySet) || now - ((KeySet) keys).fetchedAt >= KEYS_TTL_NANOS;
  }

  // Fetched keys with the nanoClock times of the last successful fetch and of the last fetch attempt.
  private static final class KeySet extends HashMap<String, Key> {
    private static final long serialVersionUID = 1L;
    private final long fetchedAt;
    private final long attemptedAt;
    private volatile boolean refreshing;

    private KeySet(Map<String, Key> keys, long fetchedAt, long attemptedAt) {
      super(keys);
      this.fetchedAt = fetchedAt;
      this.attemptedAt = attemptedAt;
    }
  }
}
