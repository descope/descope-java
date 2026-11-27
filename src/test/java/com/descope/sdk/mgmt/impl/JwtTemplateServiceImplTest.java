package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwttemplate.ApplyJwtTemplateFromLibraryRequest;
import com.descope.model.jwttemplate.JwtTemplate;
import com.descope.model.jwttemplate.JwtTemplateLibraryEntry;
import com.descope.model.jwttemplate.JwtTemplateValidationIssue;
import com.descope.model.jwttemplate.JwtTemplateValidationResponse;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.JwtTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

class JwtTemplateServiceImplTest {

  private static final String MANAGEMENT_KEY = "someManagementKey";
  private static final String TEMPLATE_JSON = "{\"id\":\"T1\",\"name\":\"my-template\",\"description\":\"desc\","
      + "\"template\":{\"claim\":\"value\"},\"source\":\"library\",\"tags\":[\"tag1\"],\"authSchema\":\"tenantOnly\","
      + "\"type\":\"user\",\"conformanceIssuer\":true,\"autoDCT\":true,\"enforceIssuer\":true,"
      + "\"emptyClaimPolicy\":\"delete\",\"overrideSubject\":true,\"issuerType\":\"federated\","
      + "\"omitCustomClaimsFromDSR\":true,\"addJti\":true,\"excludePermissions\":true,\"unknownField\":1}";
  private static final String ENTRY_JSON = "{\"id\":\"L1\",\"name\":\"starter\",\"description\":\"desc\","
      + "\"template\":{\"claim\":\"value\"},\"tags\":[\"tag1\"],\"authSchema\":\"default\",\"type\":\"key\","
      + "\"conformanceIssuer\":true,\"autoDCT\":true,\"enforceIssuer\":true,\"emptyClaimPolicy\":\"nil\","
      + "\"overrideSubject\":true,\"issuerType\":\"inbound\",\"omitCustomClaimsFromDSR\":true,\"addJti\":true,"
      + "\"excludePermissions\":true,\"experimental\":true,\"logoLight\":\"light\",\"logoDark\":\"dark\"}";

  private JwtTemplateService jwtTemplateService;
  private ArgumentCaptor<ClassicHttpRequest> captor;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    client.setManagementKey(MANAGEMENT_KEY);
    jwtTemplateService = new JwtTemplateServiceImpl(client);
    captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
  }

  @Test
  void testCreate() throws Exception {
    JwtTemplate template =
        call("{\"template\":" + TEMPLATE_JSON + "}", () -> jwtTemplateService.create(fullTemplate()));
    Map<?, ?> body = sentBody("/v1/mgmt/jwt/templates/create");
    assertEquals(new ObjectMapper().readValue(TEMPLATE_JSON.replace(",\"unknownField\":1", ""), Map.class),
        body.get("template"));
    assertEquals(1, body.size());
    assertFullTemplate(template);
  }

  @Test
  void testCreateOmitsUnsetFields() throws Exception {
    call("{\"template\":{\"id\":\"T1\"}}", () -> jwtTemplateService.create(
        JwtTemplate.builder().name("n").type(JwtTemplate.TYPE_KEY).build()));
    assertEquals(mapOf("template", mapOf("name", "n", "type", "key")), sentBody("/v1/mgmt/jwt/templates/create"));
  }

  @Test
  void testCreateInvalid() {
    assertInvalid("template", () -> jwtTemplateService.create(null));
    assertInvalid("name", () -> jwtTemplateService.create(JwtTemplate.builder().type("user").build()));
    assertInvalid("type", () -> jwtTemplateService.create(JwtTemplate.builder().name("n").build()));
  }

  @Test
  void testUpdate() throws Exception {
    JwtTemplate template =
        call("{\"template\":" + TEMPLATE_JSON + "}", () -> jwtTemplateService.update(fullTemplate()));
    Map<?, ?> body = sentBody("/v1/mgmt/jwt/templates/update");
    assertEquals("T1", ((Map<?, ?>) body.get("template")).get("id"));
    assertFullTemplate(template);
  }

  @Test
  void testUpdateInvalid() {
    assertInvalid("template", () -> jwtTemplateService.update(null));
    assertInvalid("name", () -> jwtTemplateService.update(JwtTemplate.builder().id("T1").type("user").build()));
    assertInvalid("type", () -> jwtTemplateService.update(JwtTemplate.builder().id("T1").name("n").build()));
  }

  @Test
  void testLoad() throws Exception {
    JwtTemplate template = call("{\"template\":" + TEMPLATE_JSON + "}", () -> jwtTemplateService.load("T1"));
    assertEquals(mapOf("id", "T1"), sentBody("/v1/mgmt/jwt/templates/load"));
    assertFullTemplate(template);
    assertInvalid("id", () -> jwtTemplateService.load(null));
  }

  @Test
  void testLoadMissingTemplate() throws Exception {
    assertNull(call("{\"template\":null}", () -> jwtTemplateService.load("T1")));
  }

  @Test
  void testList() throws Exception {
    List<JwtTemplate> templates = call("{\"templates\":[" + TEMPLATE_JSON + "]}", () -> jwtTemplateService.list());
    assertEquals(Collections.emptyMap(), sentBody("/v1/mgmt/jwt/templates/list"));
    assertEquals(1, templates.size());
    assertFullTemplate(templates.get(0));
    assertTrue(call("{}", () -> jwtTemplateService.list()).isEmpty());
  }

  @Test
  void testValidate() throws Exception {
    JwtTemplateValidationResponse resp = call("{\"valid\":false,\"issues\":[{\"field\":\"name\","
        + "\"code\":\"required\",\"message\":\"name is required\",\"hint\":\"set a name\"}]}",
        () -> jwtTemplateService.validate("T1", fullTemplate()));
    Map<?, ?> body = sentBody("/v1/mgmt/jwt/templates/validate");
    assertEquals("T1", body.get("id"));
    assertEquals("my-template", ((Map<?, ?>) body.get("template")).get("name"));
    assertFalse(resp.getValid());
    JwtTemplateValidationIssue issue = resp.getIssues().get(0);
    assertEquals("name", issue.getField());
    assertEquals("required", issue.getCode());
    assertEquals("name is required", issue.getMessage());
    assertEquals("set a name", issue.getHint());

    call("{\"valid\":true}", () -> jwtTemplateService.validate(null, fullTemplate()));
    assertFalse(sentBody("/v1/mgmt/jwt/templates/validate").containsKey("id"));
    call("{\"valid\":true}", () -> jwtTemplateService.validate("T1", null));
    assertEquals(mapOf("id", "T1"), sentBody("/v1/mgmt/jwt/templates/validate"));
  }

  @Test
  void testValidateInvalid() {
    assertInvalid("template", () -> jwtTemplateService.validate("", null));
  }

  @Test
  void testListLibrary() throws Exception {
    List<JwtTemplateLibraryEntry> entries = call("{\"entries\":[" + ENTRY_JSON + "]}",
        () -> jwtTemplateService.listLibrary());
    assertEquals(Collections.emptyMap(), sentBody("/v1/mgmt/jwt/templates/library/list"));
    assertEquals(1, entries.size());
    assertFullEntry(entries.get(0));
    assertTrue(call("{}", () -> jwtTemplateService.listLibrary()).isEmpty());
  }

  @Test
  void testLoadLibraryEntry() throws Exception {
    JwtTemplateLibraryEntry entry = call("{\"entry\":" + ENTRY_JSON + "}",
        () -> jwtTemplateService.loadLibraryEntry("L1"));
    assertEquals(mapOf("id", "L1"), sentBody("/v1/mgmt/jwt/templates/library/load"));
    assertFullEntry(entry);
    assertInvalid("id", () -> jwtTemplateService.loadLibraryEntry(" "));
  }

  @Test
  void testApplyFromLibrary() throws Exception {
    JwtTemplate template = call("{\"template\":" + TEMPLATE_JSON + "}",
        () -> jwtTemplateService.applyFromLibrary(ApplyJwtTemplateFromLibraryRequest.builder()
            .libraryEntryId("L1").nameOverride("name").descriptionOverride("desc")
            .tagsOverride(Arrays.asList("tag1")).templateOverride(mapOf("claim", "value")).build()));
    assertEquals(mapOf("libraryEntryId", "L1", "nameOverride", "name", "descriptionOverride", "desc",
        "tagsOverride", Arrays.asList("tag1"), "templateOverride", mapOf("claim", "value")),
        sentBody("/v1/mgmt/jwt/templates/library/apply"));
    assertFullTemplate(template);

    call("{}", () -> jwtTemplateService.applyFromLibrary(
        ApplyJwtTemplateFromLibraryRequest.builder().libraryEntryId("L1").build()));
    assertEquals(mapOf("libraryEntryId", "L1"), sentBody("/v1/mgmt/jwt/templates/library/apply"));
  }

  @Test
  void testApplyFromLibraryInvalid() {
    assertInvalid("request", () -> jwtTemplateService.applyFromLibrary(null));
    assertInvalid("libraryEntryId",
        () -> jwtTemplateService.applyFromLibrary(ApplyJwtTemplateFromLibraryRequest.builder().build()));
  }

  private static JwtTemplate fullTemplate() {
    return JwtTemplate.builder().id("T1").name("my-template").description("desc")
        .template(mapOf("claim", "value")).source("library").tags(Arrays.asList("tag1"))
        .authSchema(JwtTemplate.AUTH_SCHEMA_TENANT_ONLY).type(JwtTemplate.TYPE_USER).conformanceIssuer(true)
        .autoDCT(true).enforceIssuer(true).emptyClaimPolicy(JwtTemplate.EMPTY_CLAIM_POLICY_DELETE)
        .overrideSubject(true).issuerType(JwtTemplate.ISSUER_TYPE_FEDERATED).omitCustomClaimsFromDSR(true)
        .addJti(true).excludePermissions(true).build();
  }

  private static void assertFullTemplate(JwtTemplate template) {
    assertEquals(fullTemplate(), template);
  }

  private static void assertFullEntry(JwtTemplateLibraryEntry entry) {
    assertEquals(JwtTemplateLibraryEntry.builder().id("L1").name("starter").description("desc")
        .template(mapOf("claim", "value")).tags(Arrays.asList("tag1")).authSchema(JwtTemplate.AUTH_SCHEMA_DEFAULT)
        .type(JwtTemplate.TYPE_KEY).conformanceIssuer(true).autoDCT(true).enforceIssuer(true)
        .emptyClaimPolicy(JwtTemplate.EMPTY_CLAIM_POLICY_NIL).overrideSubject(true)
        .issuerType(JwtTemplate.ISSUER_TYPE_INBOUND).omitCustomClaimsFromDSR(true).addJti(true)
        .excludePermissions(true).experimental(true).logoLight("light").logoDark("dark").build(), entry);
  }

  // Fails on the argument check, before any request is sent.
  private void assertInvalid(String argument, Supplier<?> call) {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      ServerCommonException thrown = assertThrows(ServerCommonException.class, call::get);
      assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
      verify(httpClient, never()).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> T call(String responseBody, Supplier<T> call) throws IOException {
    CloseableHttpClient httpClient = mockHttpClient(responseBody);
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      return call.get();
    }
  }

  // Checks the last request's method, path and credential, and returns its JSON body.
  private Map<?, ?> sentBody(String path) throws Exception {
    ClassicHttpRequest request = captor.getValue();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith(path));
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY,
        request.getHeader("Authorization").getValue());
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
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
