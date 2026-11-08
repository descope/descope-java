package com.descope.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.DescopeException;
import com.descope.sdk.auth.impl.KeyProvider;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ClientKeyCacheTest {

  private static final long TTL_NANOS = TimeUnit.MINUTES.toNanos(10);
  private static final long COOLDOWN_NANOS = TimeUnit.SECONDS.toNanos(30);

  private final AtomicLong now = new AtomicLong(1_000_000_000L);
  private final AtomicInteger fetches = new AtomicInteger();
  private volatile String body;
  private volatile int status = 200;
  private volatile long delayMs = 0;
  private LongSupplier originalClock;
  private HttpServer server;
  private Client client;

  @BeforeEach
  void setUp() throws IOException {
    originalClock = Client.nanoClock;
    Client.nanoClock = now::get;
    body = jwks("k1");
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v2/keys/P123", exchange -> {
      fetches.incrementAndGet();
      try {
        Thread.sleep(delayMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = Client.builder()
        .uri("http://127.0.0.1:" + server.getAddress().getPort())
        .projectId("P123")
        .build();
  }

  @AfterEach
  void tearDown() {
    Client.nanoClock = originalClock;
    server.stop(0);
  }

  @Test
  void sameKidWithinTtlFetchesOnce() {
    assertNotNull(client.getKey("k1"));
    now.addAndGet(TTL_NANOS - 1);
    assertNotNull(client.getKey("k1"));
    assertEquals(1, fetches.get());
  }

  @Test
  void keysAreReplacedAfterTtl() {
    assertNotNull(client.getKey("k1"));
    body = jwks("k2");
    now.addAndGet(TTL_NANOS - 1);
    assertNotNull(client.getKey("k1"), "key must stay valid until the TTL passes");
    now.addAndGet(1);
    assertNull(client.getKey("k1"), "a key no longer published must not survive a refresh");
    assertNotNull(client.getKey("k2"));
    assertEquals(2, fetches.get());
  }

  @Test
  void unknownKidRefetchesOncePerCooldown() {
    assertNotNull(client.getKey("k1"));
    assertNull(client.getKey("nope"));
    assertNull(client.getKey("other"));
    assertEquals(1, fetches.get(), "unknown kids right after a fetch must not hit the network");
    now.addAndGet(COOLDOWN_NANOS);
    assertNull(client.getKey("nope"));
    assertNull(client.getKey("other"));
    assertEquals(2, fetches.get());
  }

  @Test
  void failedRefreshKeepsKeysAndThrottlesTheirRefresh() {
    assertNotNull(client.getKey("k1"));
    status = 400;
    now.addAndGet(TTL_NANOS);
    assertNotNull(client.getKey("k1"), "cached key must stay usable when the refresh fails");
    assertNotNull(client.getKey("k1"));
    assertEquals(2, fetches.get(), "a failed refresh must not be retried for cached keys within the cooldown");
    assertThrows(DescopeException.class, () -> client.getKey("nope"));
    assertEquals(3, fetches.get(), "a failed fetch must not delay unknown kids");
    assertNotNull(client.getKey("k1"));
    assertEquals(3, fetches.get());
    now.addAndGet(COOLDOWN_NANOS);
    assertNotNull(client.getKey("k1"));
    assertEquals(4, fetches.get());
    status = 200;
    body = jwks("k2");
    now.addAndGet(COOLDOWN_NANOS);
    assertNotNull(client.getKey("k2"));
    assertNull(client.getKey("k1"));
  }

  @Test
  void cachedKeysAreRejectedAnHourAfterLastSuccessfulFetch() {
    assertNotNull(client.getKey("k1"));
    status = 400;
    now.addAndGet(TimeUnit.HOURS.toNanos(1) - 1);
    assertNotNull(client.getKey("k1"), "cached key must stay usable within an hour of the last successful fetch");
    assertEquals(2, fetches.get());
    now.addAndGet(1);
    assertThrows(DescopeException.class, () -> client.getKey("k1"));
    assertEquals(3, fetches.get());
  }

  @Test
  void failedFetchDoesNotDelayUnknownKid() {
    status = 400;
    assertThrows(DescopeException.class, () -> client.getKey("k1"));
    status = 200;
    assertNotNull(client.getKey("k1"), "the next lookup after a failed fetch must retry");
    assertEquals(2, fetches.get());
    assertNull(client.getKey("nope"));
    assertEquals(2, fetches.get(), "unknown kids right after a successful fetch must not hit the network");
  }

  @Test
  void emptyResponseReplacesKeySet() {
    assertNotNull(client.getKey("k1"));
    body = "{\"keys\":[]}";
    now.addAndGet(TTL_NANOS);
    assertNull(client.getKey("k1"));
    assertEquals(2, fetches.get());
  }

  @Test
  void concurrentLookupsShareOneFetch() throws Exception {
    delayMs = 200;
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      CountDownLatch start = new CountDownLatch(1);
      List<Future<Key>> results = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        results.add(pool.submit(() -> {
          start.await();
          return client.getKey("k1");
        }));
      }
      start.countDown();
      for (Future<Key> f : results) {
        assertNotNull(f.get(10, TimeUnit.SECONDS));
      }
      assertEquals(1, fetches.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void concurrentLookupsShareOneFailedFetch() throws Exception {
    status = 400;
    delayMs = 500;
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      CountDownLatch start = new CountDownLatch(1);
      List<Future<Key>> results = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        results.add(pool.submit(() -> {
          start.await();
          try {
            return client.getKey("k1");
          } catch (DescopeException e) {
            return null;
          }
        }));
      }
      start.countDown();
      for (Future<Key> f : results) {
        assertNull(f.get(10, TimeUnit.SECONDS));
      }
      assertEquals(1, fetches.get(), "callers waiting on a failed fetch must not each fetch again");
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void staleCachedKeyIsServedWhileAnotherCallerRefreshes() throws Exception {
    assertNotNull(client.getKey("k1"));
    now.addAndGet(TTL_NANOS);
    delayMs = 2000;
    ExecutorService pool = Executors.newSingleThreadExecutor();
    try {
      final Future<Key> refresh = pool.submit(() -> client.getKey("k1"));
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (fetches.get() < 2 && System.nanoTime() < deadline) {
        Thread.yield();
      }
      assertEquals(2, fetches.get());
      assertNotNull(client.getKey("k1"));
      assertFalse(refresh.isDone(), "the cached key must be returned without waiting for the refresh");
      assertNotNull(refresh.get(10, TimeUnit.SECONDS));
      assertEquals(2, fetches.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void staleKeysStillRefreshAfterFetchThrowsError() {
    assertNotNull(client.getKey("k1"));
    now.addAndGet(TTL_NANOS);
    try (MockedStatic<KeyProvider> keyProvider = mockStatic(KeyProvider.class)) {
      keyProvider.when(() -> KeyProvider.getKeys(any(), any(), any())).thenThrow(new StackOverflowError());
      assertThrows(StackOverflowError.class, () -> client.getKey("k1"));
    }
    body = jwks("k2");
    assertNull(client.getKey("k1"), "an interrupted refresh must not leave stale keys served forever");
    assertEquals(2, fetches.get());
  }

  @Test
  void providedKeyIsNeverFetched() throws NoSuchAlgorithmException {
    Key provided = newKeyPair().getPublic();
    client.setProvidedKey(provided);
    now.addAndGet(TTL_NANOS * 2);
    assertEquals(provided, client.getKey("anything"));
    assertEquals(0, fetches.get());
  }

  private static KeyPair newKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    return gen.generateKeyPair();
  }

  private static String jwks(String kid) {
    try {
      RSAPublicKey pub = (RSAPublicKey) newKeyPair().getPublic();
      return "{\"keys\":[{\"alg\":\"RS256\",\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"" + kid
          + "\",\"e\":\"" + b64(pub.getPublicExponent()) + "\",\"n\":\"" + b64(pub.getModulus()) + "\"}]}";
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String b64(BigInteger v) {
    byte[] bytes = v.toByteArray();
    int skip = bytes.length > 1 && bytes[0] == 0 ? 1 : 0;
    byte[] trimmed = new byte[bytes.length - skip];
    System.arraycopy(bytes, skip, trimmed, 0, trimmed.length);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(trimmed);
  }
}
