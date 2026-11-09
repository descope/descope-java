package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttribute;
import com.descope.model.customattributes.CustomAttributeOption;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.model.family.Family;
import com.descope.model.family.FamilySettings;
import com.descope.model.family.request.FamilyDependentRequest;
import com.descope.model.family.request.FamilyRequest;
import com.descope.model.family.request.FamilySearchRequest;
import com.descope.model.family.response.FamilyCustomAttributesResponse;
import com.descope.model.user.response.UserResponse;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FamilyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FamilyServiceImplTest {

  private static final String MANAGEMENT_KEY = "someManagementKey";
  private static final String FAMILY_JSON = "{\"id\":\"F1\",\"name\":\"Smiths\","
      + "\"customAttributes\":{\"k\":\"v\"},\"disabled\":true,\"photo\":\"https://photo\","
      + "\"createdTime\":1700000000}";
  private static final String CUSTOM_ATTRIBUTE_JSON = "{\"name\":\"color\",\"type\":4,\"displayName\":\"Color\","
      + "\"options\":[{\"value\":\"r\",\"label\":\"Red\"}],\"defaultValue\":{\"@type\":\"t\",\"value\":\"r\"},"
      + "\"viewPermissions\":[\"view\"],\"editPermissions\":[\"edit\"],\"editable\":true}";
  private static final String CUSTOM_ATTRIBUTES_JSON =
      "{\"data\":[" + CUSTOM_ATTRIBUTE_JSON + "],\"total\":\"8416030737\"}";

  private static final CustomAttribute CUSTOM_ATTRIBUTE = CustomAttribute.builder()
      .name("color")
      .type(4)
      .displayName("Color")
      .options(Arrays.asList(CustomAttributeOption.builder().value("r").label("Red").build()))
      .defaultValue(mapOf("@type", "t", "value", "r"))
      .viewPermissions(Arrays.asList("view"))
      .editPermissions(Arrays.asList("edit"))
      .editable(true)
      .build();

  private final ObjectMapper mapper = new ObjectMapper();
  private HttpServer server;
  private String responseBody;
  private String method;
  private String path;
  private String authorization;
  private String requestBody;
  private FamilyService familyService;

  @BeforeEach
  void setUp() throws IOException {
    responseBody = "{}";
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      method = exchange.getRequestMethod();
      path = exchange.getRequestURI().getPath();
      authorization = exchange.getRequestHeaders().getFirst("Authorization");
      requestBody = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
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
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    familyService = new FamilyServiceImpl(client);
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  private void assertRequest(String expectedMethod, String expectedPath, String expectedBody)
      throws IOException {
    assertEquals(expectedMethod, method);
    assertEquals(expectedPath, path);
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY, authorization);
    if (expectedBody != null) {
      assertEquals(mapper.readTree(expectedBody), mapper.readTree(requestBody));
    }
  }

  private void assertFamily(Family family) {
    assertEquals("F1", family.getId());
    assertEquals("Smiths", family.getName());
    assertEquals(Collections.singletonMap("k", "v"), family.getCustomAttributes());
    assertTrue(family.getDisabled());
    assertEquals("https://photo", family.getPhoto());
    assertEquals(1700000000, family.getCreatedTime());
  }

  private void assertCustomAttributes(FamilyCustomAttributesResponse response) {
    assertEquals(8416030737L, response.getTotal());
    assertEquals(CUSTOM_ATTRIBUTE, response.getData().get(0));
  }

  private void assertInvalid(String argument, Runnable call) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, call::run);
    assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
    assertNull(method);
  }

  @Test
  void testCreate() throws IOException {
    responseBody = "{\"family\":" + FAMILY_JSON + "}";
    Family family = familyService.create(FamilyRequest.builder()
        .name("Smiths")
        .customAttributes(Collections.singletonMap("k", "v"))
        .photo("https://photo")
        .disabled(true)
        .build());
    assertRequest("POST", "/v1/mgmt/family/create",
        "{\"name\":\"Smiths\",\"customAttributes\":{\"k\":\"v\"},\"photo\":\"https://photo\",\"disabled\":true}");
    assertFamily(family);
  }

  @Test
  void testCreateWithId() throws IOException {
    responseBody = "{\"family\":" + FAMILY_JSON + "}";
    Family family = familyService.createWithId("F1", FamilyRequest.builder().name("Smiths").build());
    assertRequest("POST", "/v1/mgmt/family/create", "{\"familyId\":\"F1\",\"name\":\"Smiths\"}");
    assertFamily(family);
  }

  @Test
  void testCreateInvalid() {
    assertInvalid("name", () -> familyService.create(null));
    assertInvalid("name", () -> familyService.create(FamilyRequest.builder().name("").build()));
    assertInvalid("id", () -> familyService.createWithId("", FamilyRequest.builder().name("n").build()));
  }

  @Test
  void testUpdate() throws IOException {
    responseBody = "{\"family\":" + FAMILY_JSON + "}";
    Family family = familyService.update("F1", FamilyRequest.builder().name("Smiths").disabled(false).build());
    assertRequest("POST", "/v1/mgmt/family/update", "{\"id\":\"F1\",\"name\":\"Smiths\",\"disabled\":false}");
    assertFamily(family);
  }

  @Test
  void testUpdateInvalid() {
    assertInvalid("id", () -> familyService.update(" ", FamilyRequest.builder().build()));
  }

  @Test
  void testDelete() throws IOException {
    familyService.delete("F1");
    assertRequest("POST", "/v1/mgmt/family/delete", "{\"id\":\"F1\"}");
  }

  @Test
  void testDeleteInvalid() {
    assertInvalid("id", () -> familyService.delete(""));
  }

  @Test
  void testSearchAll() throws IOException {
    responseBody = "{\"families\":[" + FAMILY_JSON + "]}";
    List<Family> families = familyService.searchAll(FamilySearchRequest.builder()
        .ids(Arrays.asList("F1"))
        .names(Arrays.asList("Smiths"))
        .freeText("smi")
        .customAttributes(Collections.singletonMap("k", "v"))
        .page(1)
        .size(10)
        .build());
    assertRequest("POST", "/v1/mgmt/family/search", "{\"familyIds\":[\"F1\"],\"familyNames\":[\"Smiths\"],"
        + "\"freeText\":\"smi\",\"customAttributes\":{\"k\":\"v\"},\"page\":1,\"size\":10}");
    assertEquals(1, families.size());
    assertFamily(families.get(0));
  }

  @Test
  void testSearchAllNullRequest() throws IOException {
    responseBody = "{\"families\":[]}";
    List<Family> families = familyService.searchAll(null);
    assertRequest("POST", "/v1/mgmt/family/search", "{}");
    assertTrue(families.isEmpty());
  }

  @Test
  void testCreateDependent() throws IOException {
    responseBody = "{\"user\":{\"userId\":\"U1\",\"loginIds\":[\"kid\"],\"name\":\"Kid\",\"email\":\"kid@x.com\","
        + "\"phone\":\"+15555555555\",\"givenName\":\"K\",\"middleName\":\"I\",\"familyName\":\"D\","
        + "\"picture\":\"https://pic\",\"customAttributes\":{\"a\":\"b\"},\"status\":\"enabled\"}}";
    Map<String, Object> scoped = Collections.singletonMap("grade", Collections.singletonMap("value", "5"));
    UserResponse user = familyService.createDependent("F1", FamilyDependentRequest.builder()
        .loginId("kid")
        .name("Kid")
        .email("kid@x.com")
        .phone("+15555555555")
        .givenName("K")
        .middleName("I")
        .familyName("D")
        .picture("https://pic")
        .customAttributes(Collections.singletonMap("a", "b"))
        .familyScopedAttributes(scoped)
        .build());
    assertRequest("POST", "/v1/mgmt/family/dependent/create", "{\"familyId\":\"F1\",\"loginId\":\"kid\","
        + "\"name\":\"Kid\",\"email\":\"kid@x.com\",\"phone\":\"+15555555555\",\"givenName\":\"K\","
        + "\"middleName\":\"I\",\"familyName\":\"D\",\"picture\":\"https://pic\",\"customAttributes\":{\"a\":\"b\"},"
        + "\"familyScopedAttributes\":{\"grade\":{\"value\":\"5\"}}}");
    assertEquals("U1", user.getUserId());
    assertEquals(Arrays.asList("kid"), user.getLoginIds());
    assertEquals("Kid", user.getName());
    assertEquals("kid@x.com", user.getEmail());
    assertEquals("+15555555555", user.getPhone());
    assertEquals("K", user.getGivenName());
    assertEquals("I", user.getMiddleName());
    assertEquals("D", user.getFamilyName());
    assertEquals("https://pic", user.getPicture());
    assertEquals(Collections.singletonMap("a", "b"), user.getCustomAttributes());
    assertEquals("enabled", user.getStatus());
  }

  @Test
  void testCreateDependentOnlyFamily() throws IOException {
    responseBody = "{\"user\":{\"userId\":\"U1\"}}";
    UserResponse user = familyService.createDependent("F1", null);
    assertRequest("POST", "/v1/mgmt/family/dependent/create", "{\"familyId\":\"F1\"}");
    assertEquals("U1", user.getUserId());
  }

  @Test
  void testCreateDependentInvalid() {
    assertInvalid("familyId", () -> familyService.createDependent("", FamilyDependentRequest.builder().build()));
  }

  @Test
  void testDeleteDependent() throws IOException {
    familyService.deleteDependent("U1");
    assertRequest("POST", "/v1/mgmt/family/dependent/delete", "{\"userId\":\"U1\"}");
  }

  @Test
  void testDeleteDependentInvalid() {
    assertInvalid("userId", () -> familyService.deleteDependent(null));
  }

  @Test
  void testImpersonateDependent() throws IOException {
    responseBody = "{\"jwt\":\"impersonated-jwt\"}";
    String jwt = familyService.impersonateDependent("admin", "kid", "F1");
    assertRequest("POST", "/v1/mgmt/family/impersonate",
        "{\"impersonatorUserIdOrLoginId\":\"admin\",\"dependentLoginId\":\"kid\",\"selectedFamily\":\"F1\"}");
    assertEquals("impersonated-jwt", jwt);
  }

  @Test
  void testImpersonateDependentWithoutFamily() throws IOException {
    responseBody = "{\"jwt\":\"impersonated-jwt\"}";
    familyService.impersonateDependent("admin", "kid", null);
    assertRequest("POST", "/v1/mgmt/family/impersonate",
        "{\"impersonatorUserIdOrLoginId\":\"admin\",\"dependentLoginId\":\"kid\"}");
  }

  @Test
  void testImpersonateDependentInvalid() {
    assertInvalid("impersonatorUserIdOrLoginId", () -> familyService.impersonateDependent("", "kid", null));
    assertInvalid("dependentLoginId", () -> familyService.impersonateDependent("admin", "", null));
  }

  @Test
  void testStopImpersonation() throws IOException {
    responseBody = "{\"jwt\":\"own-jwt\"}";
    String jwt = familyService.stopImpersonation("impersonated-jwt", Collections.singletonMap("c", "d"), 3600);
    assertRequest("POST", "/v1/mgmt/family/impersonate/stop",
        "{\"jwt\":\"impersonated-jwt\",\"customClaims\":{\"c\":\"d\"},\"refreshDuration\":3600}");
    assertEquals("own-jwt", jwt);
  }

  @Test
  void testStopImpersonationOnlyJwt() throws IOException {
    responseBody = "{\"jwt\":\"own-jwt\"}";
    familyService.stopImpersonation("impersonated-jwt", null, null);
    assertRequest("POST", "/v1/mgmt/family/impersonate/stop", "{\"jwt\":\"impersonated-jwt\"}");
  }

  @Test
  void testStopImpersonationInvalid() {
    assertInvalid("jwt", () -> familyService.stopImpersonation("", null, null));
  }

  @Test
  void testGetSettings() throws IOException {
    responseBody = "{\"enabled\":true,\"maxFamilyMembers\":6,\"allowMultipleFamiliesUsers\":false}";
    FamilySettings settings = familyService.getSettings();
    assertRequest("GET", "/v1/mgmt/family/settings", null);
    assertTrue(settings.getEnabled());
    assertEquals(6, settings.getMaxFamilyMembers());
    assertFalse(settings.getAllowMultipleFamiliesUsers());
  }

  @Test
  void testConfigureSettings() throws IOException {
    responseBody = "{\"enabled\":true,\"maxFamilyMembers\":8,\"allowMultipleFamiliesUsers\":true}";
    FamilySettings settings = familyService.configureSettings(FamilySettings.builder()
        .enabled(true)
        .maxFamilyMembers(8)
        .allowMultipleFamiliesUsers(true)
        .build());
    assertRequest("POST", "/v1/mgmt/family/settings",
        "{\"enabled\":true,\"maxFamilyMembers\":8,\"allowMultipleFamiliesUsers\":true}");
    assertTrue(settings.getEnabled());
    assertEquals(8, settings.getMaxFamilyMembers());
    assertTrue(settings.getAllowMultipleFamiliesUsers());
  }

  @Test
  void testConfigureSettingsPartial() throws IOException {
    responseBody = "{\"enabled\":true,\"maxFamilyMembers\":8,\"allowMultipleFamiliesUsers\":true}";
    familyService.configureSettings(FamilySettings.builder().maxFamilyMembers(8).build());
    assertRequest("POST", "/v1/mgmt/family/settings", "{\"maxFamilyMembers\":8}");
  }

  @Test
  void testConfigureSettingsInvalid() {
    assertInvalid("settings", () -> familyService.configureSettings(null));
  }

  @Test
  void testGetCustomAttributes() throws IOException {
    responseBody = CUSTOM_ATTRIBUTES_JSON;
    FamilyCustomAttributesResponse response = familyService.getCustomAttributes();
    assertRequest("GET", "/v1/mgmt/family/customattributes", null);
    assertCustomAttributes(response);
  }

  @Test
  void testCreateCustomAttributes() throws IOException {
    responseBody = CUSTOM_ATTRIBUTES_JSON;
    FamilyCustomAttributesResponse response = familyService.createCustomAttributes(
        CreateCustomAttributesRequest.builder().attributes(Arrays.asList(CUSTOM_ATTRIBUTE)).build());
    assertRequest("POST", "/v1/mgmt/family/customattribute/create",
        "{\"attributes\":[" + CUSTOM_ATTRIBUTE_JSON + "]}");
    assertCustomAttributes(response);
  }

  @Test
  void testCreateCustomAttributesInvalid() {
    assertInvalid("Request", () -> familyService.createCustomAttributes(null));
    assertInvalid("attribute name", () -> familyService.createCustomAttributes(CreateCustomAttributesRequest
        .builder().attributes(Arrays.asList(CustomAttribute.builder().name("").type(1).build())).build()));
  }

  @Test
  void testDeleteCustomAttributes() throws IOException {
    responseBody = CUSTOM_ATTRIBUTES_JSON;
    FamilyCustomAttributesResponse response = familyService.deleteCustomAttributes(
        DeleteCustomAttributesRequest.builder().names(Arrays.asList("old")).build());
    assertRequest("POST", "/v1/mgmt/family/customattribute/delete", "{\"names\":[\"old\"]}");
    assertCustomAttributes(response);
  }

  @Test
  void testDeleteCustomAttributesInvalid() {
    assertInvalid("Request", () -> familyService.deleteCustomAttributes(null));
    assertInvalid("names", () -> familyService.deleteCustomAttributes(DeleteCustomAttributesRequest.builder()
        .names(Arrays.asList("")).build()));
  }
}
