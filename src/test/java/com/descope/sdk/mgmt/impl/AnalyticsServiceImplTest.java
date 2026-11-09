package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.descope.model.analytics.AnalyticRecord;
import com.descope.model.analytics.AnalyticsSearchRequest;
import com.descope.model.analytics.AnalyticsSearchResponse;
import com.descope.model.client.Client;
import com.descope.sdk.mgmt.AnalyticsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class AnalyticsServiceImplTest {
  private static final String RESPONSE_BODY = "{\"analytics\":[{\"projectId\":\"P123\",\"action\":\"LoginSucceed\","
      + "\"created\":\"1700000000000\",\"device\":\"Desktop\",\"method\":\"otp\",\"geo\":\"US\","
      + "\"tenant\":\"t1\",\"cnt\":\"42\",\"referrer\":\"https://example.com\"},"
      + "{\"created\":1700000001000,\"cnt\":7,\"unknown\":\"x\"}]}";

  private final AtomicReference<String> method = new AtomicReference<>();
  private final AtomicReference<String> path = new AtomicReference<>();
  private final AtomicReference<String> authorization = new AtomicReference<>();
  private final AtomicReference<String> body = new AtomicReference<>();
  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void testSearchForSuccess() throws Exception {
    AnalyticsService service = serviceReturning(RESPONSE_BODY);
    AnalyticsSearchResponse resp = service.search(AnalyticsSearchRequest.builder()
        .from(Instant.ofEpochMilli(1690000000000L))
        .to(Instant.ofEpochMilli(1700000000000L))
        .actions(Arrays.asList("LoginSucceed"))
        .excludedActions(Arrays.asList("Logout"))
        .devices(Arrays.asList("Desktop"))
        .methods(Arrays.asList("otp"))
        .geos(Arrays.asList("US"))
        .tenants(Arrays.asList("t1"))
        .groupByAction(true)
        .groupByDevice(false)
        .groupByMethod(true)
        .groupByGeo(true)
        .groupByTenant(true)
        .groupByReferrer(true)
        .groupByCreated("d")
        .build());

    assertEquals("POST", method.get());
    assertEquals("/v1/mgmt/analytics/search", path.get());
    assertEquals("Bearer P123:mgmt-key", authorization.get());
    JsonNode sent = new ObjectMapper().readTree(body.get());
    assertEquals("1690000000000", sent.get("from").asText());
    assertThat(sent.get("from").isTextual()).isTrue();
    assertEquals("1700000000000", sent.get("to").asText());
    assertEquals("LoginSucceed", sent.get("actions").get(0).asText());
    assertEquals("Logout", sent.get("excludedActions").get(0).asText());
    assertEquals("Desktop", sent.get("devices").get(0).asText());
    assertEquals("otp", sent.get("methods").get(0).asText());
    assertEquals("US", sent.get("geos").get(0).asText());
    assertEquals("t1", sent.get("tenants").get(0).asText());
    assertThat(sent.get("groupByAction").asBoolean()).isTrue();
    assertThat(sent.get("groupByDevice").asBoolean()).isFalse();
    assertThat(sent.get("groupByMethod").asBoolean()).isTrue();
    assertThat(sent.get("groupByGeo").asBoolean()).isTrue();
    assertThat(sent.get("groupByTenant").asBoolean()).isTrue();
    assertThat(sent.get("groupByReferrer").asBoolean()).isTrue();
    assertEquals("d", sent.get("groupByCreated").asText());
    assertEquals(15, sent.size());

    assertThat(resp.getAnalytics()).hasSize(2);
    AnalyticRecord rec = resp.getAnalytics().get(0);
    assertEquals("P123", rec.getProjectId());
    assertEquals("LoginSucceed", rec.getAction());
    assertEquals(Instant.ofEpochMilli(1700000000000L), rec.getCreated());
    assertEquals("Desktop", rec.getDevice());
    assertEquals("otp", rec.getMethod());
    assertEquals("US", rec.getGeo());
    assertEquals("t1", rec.getTenant());
    assertEquals("https://example.com", rec.getReferrer());
    assertEquals(42L, rec.getCnt());
    AnalyticRecord numeric = resp.getAnalytics().get(1);
    assertEquals(Instant.ofEpochMilli(1700000001000L), numeric.getCreated());
    assertEquals(7L, numeric.getCnt());
    assertThat(numeric.getAction()).isNull();
  }

  @Test
  void testSearchOmitsUnsetFields() throws Exception {
    AnalyticsService service = serviceReturning("{\"analytics\":null}");
    AnalyticsSearchResponse resp = service.search(null);
    assertEquals("{}", body.get());
    assertThat(resp.getAnalytics()).isEmpty();
  }

  @Test
  void testSearchAbsentRecordFields() throws Exception {
    AnalyticsService service = serviceReturning("{\"analytics\":[{}]}");
    AnalyticsSearchResponse resp = service.search(new AnalyticsSearchRequest());
    assertThat(resp.getAnalytics()).hasSize(1);
    assertThat(resp.getAnalytics().get(0).getCreated()).isNull();
    assertThat(resp.getAnalytics().get(0).getCnt()).isNull();
  }

  private AnalyticsService serviceReturning(String responseBody) throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      method.set(exchange.getRequestMethod());
      path.set(exchange.getRequestURI().getPath());
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      body.set(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8));
      byte[] payload = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    Client client = Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId("P123")
        .managementKey("mgmt-key")
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    return new AnalyticsServiceImpl(client);
  }
}
