package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.mgmt.AccessKeyResponseDetails;
import com.descope.model.scim.ActivateScimAccessKeysRequest;
import com.descope.model.scim.ActivateScimAccessKeysResponse;
import com.descope.model.scim.CreateScimAccessKeyRequest;
import com.descope.model.scim.LoadScimAccessKeysResponse;
import com.descope.model.scim.RevokeScimAccessKeysRequest;
import com.descope.model.scim.RevokeScimAccessKeysResponse;
import com.descope.model.scim.RotateScimAccessKeyRequest;
import com.descope.model.scim.ScimAccessKeyResponse;
import com.descope.model.scim.ScimKeyRevokeMode;
import com.descope.sdk.mgmt.ScimService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ScimServiceImplTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String KEY_JSON = "{\"id\":\"k1\",\"name\":\"scim\",\"roleNames\":[\"r1\"],"
      + "\"keyTenants\":[{\"tenantId\":\"t1\",\"roleNames\":[\"r1\"],\"tenantName\":\"Tenant\"}],"
      + "\"status\":\"active\",\"createdTime\":1700000000,\"expireTime\":1800000000,\"createdBy\":\"admin\","
      + "\"clientId\":\"c1\",\"boundUserId\":\"u1\",\"customClaims\":{\"a\":\"b\"},\"editable\":true,"
      + "\"description\":\"desc\",\"permittedIps\":[\"10.0.0.1\"],\"customAttributes\":{\"c\":\"d\"}}";
  private static final String KEY_RESPONSE_JSON = "{\"cleartext\":\"" + PROJECT_ID + ":secret\",\"key\":"
      + KEY_JSON + ",\"scimUrl\":\"https://api.descope.com/scim/v2\",\"revokedKeyIds\":[\"old1\"]}";

  private ScimService scimService;
  private ArgumentCaptor<ClassicHttpRequest> captor;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    scimService = new ScimServiceImpl(client);
    captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
  }

  @Test
  void testLoadKeys() throws Exception {
    LoadScimAccessKeysResponse resp = call("{\"keys\":[{\"key\":" + KEY_JSON + ",\"ssoId\":\"sso1\"}]}",
        () -> scimService.loadKeys("t1", "sso1"));
    ClassicHttpRequest req = sent("GET", "/v1/mgmt/scim/key");
    Map<String, String> query = query(req.getUri());
    assertEquals("t1", query.get("tenantId"));
    assertEquals("sso1", query.get("ssoId"));
    assertNull(req.getEntity());
    assertEquals(1, resp.getKeys().size());
    assertEquals("sso1", resp.getKeys().get(0).getSsoId());
    assertKey(resp.getKeys().get(0).getKey());
  }

  @Test
  void testLoadKeysWithoutSsoId() throws Exception {
    call("{}", () -> scimService.loadKeys("t1", null));
    Map<String, String> query = query(sent("GET", "/v1/mgmt/scim/key").getUri());
    assertEquals("t1", query.get("tenantId"));
    assertFalse(query.containsKey("ssoId"));
  }

  @Test
  void testLoadKeysForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> scimService.loadKeys("", "sso1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateKey() throws Exception {
    ScimAccessKeyResponse resp = call(KEY_RESPONSE_JSON, () -> scimService.createKey(
        CreateScimAccessKeyRequest.builder().tenantId("t1").ssoId("sso1").name("scim").description("desc")
            .expireTime(1800000000L).build()));
    Map<?, ?> body = body(sent("POST", "/v1/mgmt/scim/key/create"));
    assertEquals("t1", body.get("tenantId"));
    assertEquals("sso1", body.get("ssoId"));
    assertEquals("scim", body.get("name"));
    assertEquals("desc", body.get("description"));
    assertEquals("1800000000", body.get("expireTime"));
    assertKeyResponse(resp);
  }

  @Test
  void testCreateKeyOmitsUnsetFields() throws Exception {
    call(KEY_RESPONSE_JSON,
        () -> scimService.createKey(CreateScimAccessKeyRequest.builder().tenantId("t1").build()));
    Map<?, ?> body = body(sent("POST", "/v1/mgmt/scim/key/create"));
    assertEquals(Collections.singletonMap("tenantId", "t1"), body);
  }

  @Test
  void testCreateKeyForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> scimService.createKey(null));
    assertEquals("The Request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> scimService.createKey(CreateScimAccessKeyRequest.builder().ssoId("sso1").build()));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testRotateKey() throws Exception {
    ScimAccessKeyResponse resp = call(KEY_RESPONSE_JSON, () -> scimService.rotateKey(
        RotateScimAccessKeyRequest.builder().tenantId("t1").ssoId("sso1").name("scim").description("desc")
            .expireTime(1800000000L).revokeMode(ScimKeyRevokeMode.DEACTIVATE).build()));
    Map<?, ?> body = body(sent("POST", "/v1/mgmt/scim/key/rotate"));
    assertEquals("t1", body.get("tenantId"));
    assertEquals("sso1", body.get("ssoId"));
    assertEquals("scim", body.get("name"));
    assertEquals("desc", body.get("description"));
    assertEquals("1800000000", body.get("expireTime"));
    assertEquals("SCIM_KEY_REVOKE_MODE_DEACTIVATE", body.get("revokeMode"));
    assertKeyResponse(resp);
  }

  @Test
  void testRotateKeyForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> scimService.rotateKey(null));
    assertEquals("The Request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> scimService.rotateKey(RotateScimAccessKeyRequest.builder().build()));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testRevokeKeys() throws Exception {
    RevokeScimAccessKeysResponse resp = call("{\"revokedKeyIds\":[\"k1\",\"k2\"]}", () -> scimService.revokeKeys(
        RevokeScimAccessKeysRequest.builder().tenantId("t1").ssoId("sso1").revokeMode(ScimKeyRevokeMode.DELETE)
            .build()));
    Map<?, ?> body = body(sent("POST", "/v1/mgmt/scim/key/revoke"));
    assertEquals("t1", body.get("tenantId"));
    assertEquals("sso1", body.get("ssoId"));
    assertEquals("SCIM_KEY_REVOKE_MODE_DELETE", body.get("revokeMode"));
    assertEquals(Arrays.asList("k1", "k2"), resp.getRevokedKeyIds());
  }

  @Test
  void testRevokeKeysForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> scimService.revokeKeys(null));
    assertEquals("The Request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> scimService.revokeKeys(RevokeScimAccessKeysRequest.builder().tenantId(" ").build()));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testActivateKeys() throws Exception {
    ActivateScimAccessKeysResponse resp = call("{\"activatedKeyIds\":[\"k1\"]}", () -> scimService.activateKeys(
        ActivateScimAccessKeysRequest.builder().tenantId("t1").ssoId("sso1").keyIds(Arrays.asList("k1")).build()));
    Map<?, ?> body = body(sent("POST", "/v1/mgmt/scim/key/activate"));
    assertEquals("t1", body.get("tenantId"));
    assertEquals("sso1", body.get("ssoId"));
    assertEquals(Arrays.asList("k1"), body.get("keyIds"));
    assertEquals(Arrays.asList("k1"), resp.getActivatedKeyIds());
  }

  @Test
  void testActivateKeysForInvalidArguments() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> scimService.activateKeys(null));
    assertEquals("The Request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> scimService.activateKeys(
        ActivateScimAccessKeysRequest.builder().keyIds(Arrays.asList("k1")).build()));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> scimService.activateKeys(
        ActivateScimAccessKeysRequest.builder().tenantId("t1").build()));
    assertEquals("The KeyIds argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> scimService.activateKeys(
        ActivateScimAccessKeysRequest.builder().tenantId("t1").keyIds(Collections.emptyList()).build()));
    assertEquals("The KeyIds argument is invalid", thrown.getMessage());
  }

  private static void assertKeyResponse(ScimAccessKeyResponse resp) {
    assertEquals(PROJECT_ID + ":secret", resp.getCleartext());
    assertEquals("https://api.descope.com/scim/v2", resp.getScimUrl());
    assertEquals(Arrays.asList("old1"), resp.getRevokedKeyIds());
    assertKey(resp.getKey());
  }

  private static void assertKey(AccessKeyResponseDetails key) {
    assertEquals("k1", key.getId());
    assertEquals("scim", key.getName());
    assertEquals(Arrays.asList("r1"), key.getRoleNames());
    assertEquals("t1", key.getKeyTenants().get(0).getTenantId());
    assertEquals(Arrays.asList("r1"), key.getKeyTenants().get(0).getRoleNames());
    assertEquals("Tenant", key.getKeyTenants().get(0).getTenantName());
    assertEquals("active", key.getStatus());
    assertEquals(1700000000L, key.getCreatedTime());
    assertEquals(1800000000L, key.getExpireTime());
    assertEquals("admin", key.getCreatedBy());
    assertEquals("c1", key.getClientId());
    assertEquals("u1", key.getBoundUserId());
    assertEquals(Collections.singletonMap("a", "b"), key.getCustomClaims());
    assertEquals(Boolean.TRUE, key.getEditable());
    assertEquals("desc", key.getDescription());
    assertEquals(Arrays.asList("10.0.0.1"), key.getPermittedIps());
    assertEquals(Collections.singletonMap("c", "d"), key.getCustomAttributes());
  }

  private <T> T call(String responseBody, Supplier<T> fn) throws IOException {
    CloseableHttpClient httpClient = mockHttpClient(responseBody);
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      T res = fn.get();
      assertNotNull(res);
      return res;
    }
  }

  private ClassicHttpRequest sent(String method, String path) throws Exception {
    ClassicHttpRequest req = captor.getValue();
    assertEquals(method, req.getMethod());
    assertEquals(path, req.getUri().getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, req.getHeader("Authorization").getValue());
    return req;
  }

  private static Map<?, ?> body(ClassicHttpRequest req) throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    req.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }

  private static Map<String, String> query(URI uri) {
    Map<String, String> res = new HashMap<>();
    for (String kv : uri.getRawQuery().split("&")) {
      String[] parts = kv.split("=", 2);
      res.put(parts[0], parts[1]);
    }
    return res;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CloseableHttpClient mockHttpClient(String responseBody) throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    return httpClient;
  }
}
