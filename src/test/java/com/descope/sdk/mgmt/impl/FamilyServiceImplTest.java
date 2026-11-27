package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.family.Family;
import com.descope.model.family.FamilyDependent;
import com.descope.model.family.FamilySettings;
import com.descope.model.family.UserFamily;
import com.descope.model.family.request.FamilyDependentRequest;
import com.descope.model.family.request.FamilySearchRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FamilyService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class FamilyServiceImplTest {

  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String FAMILY_JSON = "{\"id\":\"F1\",\"name\":\"fam\",\"customAttributes\":{\"a\":\"b\"},"
      + "\"disabled\":true,\"photo\":\"https://p\",\"createdTime\":123,\"unknown\":1}";
  private static final String SETTINGS_JSON =
      "{\"enabled\":true,\"maxFamilyMembers\":5,\"allowMultipleFamiliesUsers\":false}";
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private FamilyService familyService;
  private ApiProxy apiProxy;
  private String authHeader;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    familyService = ManagementServiceBuilder.buildServices(client).getFamilyService();
    apiProxy = mock(ApiProxy.class);
  }

  @Test
  void testCreateForSuccess() throws Exception {
    respondToPost("{\"family\":" + FAMILY_JSON + "}");
    Family family = call(() -> familyService.create("fam", Collections.singletonMap("a", "b"), "https://p", false));
    assertPost("/v1/mgmt/family/create",
        "{\"name\":\"fam\",\"customAttributes\":{\"a\":\"b\"},\"photo\":\"https://p\",\"disabled\":false}");
    assertFamily(family);
  }

  @Test
  void testCreateWithOnlyName() throws Exception {
    respondToPost("{\"family\":" + FAMILY_JSON + "}");
    call(() -> familyService.create("fam", null, null, null));
    assertPost("/v1/mgmt/family/create", "{\"name\":\"fam\"}");
  }

  @Test
  void testCreateForEmptyName() {
    assertInvalid("name", () -> familyService.create("", null, null, null));
  }

  @Test
  void testCreateWithIdForSuccess() throws Exception {
    respondToPost("{\"family\":" + FAMILY_JSON + "}");
    Family family = call(() -> familyService.createWithId("F1", "fam", null, null, true));
    assertPost("/v1/mgmt/family/create", "{\"name\":\"fam\",\"familyId\":\"F1\",\"disabled\":true}");
    assertFamily(family);
  }

  @Test
  void testCreateWithIdForEmptyArguments() {
    assertInvalid("id", () -> familyService.createWithId("", "fam", null, null, null));
    assertInvalid("name", () -> familyService.createWithId("F1", "", null, null, null));
  }

  @Test
  void testUpdateForSuccess() throws Exception {
    respondToPost("{\"family\":" + FAMILY_JSON + "}");
    Family family = call(() -> familyService.update("F1", "fam", Collections.singletonMap("a", "b"), "", false));
    assertPost("/v1/mgmt/family/update",
        "{\"id\":\"F1\",\"name\":\"fam\",\"customAttributes\":{\"a\":\"b\"},\"photo\":\"\",\"disabled\":false}");
    assertFamily(family);
  }

  @Test
  void testUpdateWithOnlyId() throws Exception {
    respondToPost("{\"family\":" + FAMILY_JSON + "}");
    call(() -> familyService.update("F1", null, null, null, null));
    assertPost("/v1/mgmt/family/update", "{\"id\":\"F1\"}");
  }

  @Test
  void testUpdateForEmptyId() {
    assertInvalid("id", () -> familyService.update("", "fam", null, null, null));
  }

  @Test
  void testDeleteForSuccess() throws Exception {
    respondToPost("{}");
    call(() -> {
      familyService.delete("F1");
      return null;
    });
    assertPost("/v1/mgmt/family/delete", "{\"id\":\"F1\"}");
  }

  @Test
  void testDeleteForEmptyId() {
    assertInvalid("id", () -> familyService.delete(""));
  }

  @Test
  void testSearchAllForSuccess() throws Exception {
    respondToPost("{\"families\":[" + FAMILY_JSON + "]}");
    List<Family> families = call(() -> familyService.searchAll(FamilySearchRequest.builder()
        .ids(Arrays.asList("F1"))
        .names(Arrays.asList("fam"))
        .text("fa")
        .customAttributes(Collections.singletonMap("a", "b"))
        .page(1)
        .size(10)
        .build()));
    assertPost("/v1/mgmt/family/search", "{\"familyIds\":[\"F1\"],\"familyNames\":[\"fam\"],\"freeText\":\"fa\","
        + "\"customAttributes\":{\"a\":\"b\"},\"page\":1,\"size\":10}");
    assertThat(families).hasSize(1);
    assertFamily(families.get(0));
  }

  @Test
  void testSearchAllWithNullRequest() throws Exception {
    respondToPost("{\"families\":[]}");
    List<Family> families = call(() -> familyService.searchAll(null));
    assertPost("/v1/mgmt/family/search", "{}");
    assertThat(families).isEmpty();
  }

  @Test
  void testCreateDependentForSuccess() throws Exception {
    respondToPost("{\"user\":{\"userId\":\"U1\",\"loginIds\":[\"kid\"],\"name\":\"Kid\",\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"F1\",\"roleNames\":[\"r\"],\"permissions\":[\"p\"],"
        + "\"familyScopedAttributes\":{\"x\":\"y\"}}]}}");
    FamilyDependent user = call(() -> familyService.createDependent("F1", FamilyDependentRequest.builder()
        .loginId("kid")
        .name("Kid")
        .email("kid@example.com")
        .phone("+15555555555")
        .givenName("K")
        .middleName("M")
        .familyName("D")
        .picture("https://p")
        .customAttributes(Collections.singletonMap("a", "b"))
        .familyScopedAttributes(Collections.singletonMap("x", "y"))
        .build()));
    assertPost("/v1/mgmt/family/dependent/create", "{\"familyId\":\"F1\",\"loginId\":\"kid\",\"name\":\"Kid\","
        + "\"email\":\"kid@example.com\",\"phone\":\"+15555555555\",\"givenName\":\"K\",\"middleName\":\"M\","
        + "\"familyName\":\"D\",\"picture\":\"https://p\",\"customAttributes\":{\"a\":\"b\"},"
        + "\"familyScopedAttributes\":{\"F1\":{\"x\":\"y\"}}}");
    assertThat(user.getUserId()).isEqualTo("U1");
    assertThat(user.getLoginIds()).containsExactly("kid");
    assertThat(user.getName()).isEqualTo("Kid");
    assertThat(user.getDependent()).isTrue();
    UserFamily userFamily = user.getUserFamilies().get(0);
    assertThat(userFamily.getFamilyId()).isEqualTo("F1");
    assertThat(userFamily.getRoleNames()).containsExactly("r");
    assertThat(userFamily.getPermissions()).containsExactly("p");
    assertThat(userFamily.getFamilyScopedAttributes()).containsEntry("x", "y");
  }

  @Test
  void testCreateDependentWithNullRequest() throws Exception {
    respondToPost("{}");
    FamilyDependent user = call(() -> familyService.createDependent("F1", null));
    assertPost("/v1/mgmt/family/dependent/create", "{\"familyId\":\"F1\"}");
    assertThat(user).isNull();
  }

  @Test
  void testCreateDependentForEmptyFamilyId() {
    assertInvalid("familyId", () -> familyService.createDependent("", null));
  }

  @Test
  void testDeleteDependentForSuccess() throws Exception {
    respondToPost("{}");
    call(() -> {
      familyService.deleteDependent("U1");
      return null;
    });
    assertPost("/v1/mgmt/family/dependent/delete", "{\"userId\":\"U1\"}");
  }

  @Test
  void testDeleteDependentForEmptyUserId() {
    assertInvalid("userId", () -> familyService.deleteDependent(""));
  }

  @Test
  void testImpersonateDependentForSuccess() throws Exception {
    respondToPost("{\"jwt\":\"a-jwt\"}");
    String jwt = call(() -> familyService.impersonateDependent("parent", "kid", "F1"));
    assertPost("/v1/mgmt/family/impersonate",
        "{\"impersonatorUserIdOrLoginId\":\"parent\",\"dependentLoginId\":\"kid\",\"selectedFamily\":\"F1\"}");
    assertThat(jwt).isEqualTo("a-jwt");
  }

  @Test
  void testImpersonateDependentWithoutSelectedFamily() throws Exception {
    respondToPost("{\"jwt\":\"a-jwt\"}");
    call(() -> familyService.impersonateDependent("parent", "kid", null));
    assertPost("/v1/mgmt/family/impersonate",
        "{\"impersonatorUserIdOrLoginId\":\"parent\",\"dependentLoginId\":\"kid\"}");
  }

  @Test
  void testImpersonateDependentForEmptyArguments() {
    assertInvalid("impersonatorUserIdOrLoginId", () -> familyService.impersonateDependent("", "kid", null));
    assertInvalid("dependentLoginId", () -> familyService.impersonateDependent("parent", "", null));
  }

  @Test
  void testStopImpersonationForSuccess() throws Exception {
    respondToPost("{\"jwt\":\"b-jwt\"}");
    String jwt = call(() -> familyService.stopImpersonation("a-jwt", Collections.singletonMap("k", "v"), 60));
    assertPost("/v1/mgmt/family/impersonate/stop",
        "{\"jwt\":\"a-jwt\",\"customClaims\":{\"k\":\"v\"},\"refreshDuration\":60}");
    assertThat(jwt).isEqualTo("b-jwt");
  }

  @Test
  void testStopImpersonationWithOnlyJwt() throws Exception {
    respondToPost("{\"jwt\":\"b-jwt\"}");
    call(() -> familyService.stopImpersonation("a-jwt", null, null));
    assertPost("/v1/mgmt/family/impersonate/stop", "{\"jwt\":\"a-jwt\"}");
  }

  @Test
  void testStopImpersonationForEmptyJwt() {
    assertInvalid("jwt", () -> familyService.stopImpersonation("", null, null));
  }

  @Test
  void testGetSettingsForSuccess() throws Exception {
    doAnswer(inv -> parse(SETTINGS_JSON, inv.getArgument(1))).when(apiProxy).get(any(), any());
    final FamilySettings settings = call(() -> familyService.getSettings());
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), any());
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/family/settings");
    assertThat(uri.getValue().getQuery()).isNull();
    assertAuthHeader();
    assertSettings(settings);
  }

  @Test
  void testConfigureSettingsForSuccess() throws Exception {
    respondToPost(SETTINGS_JSON);
    FamilySettings settings = call(() -> familyService.configureSettings(FamilySettings.builder()
        .enabled(true)
        .maxFamilyMembers(5)
        .allowMultipleFamiliesUsers(false)
        .build()));
    assertPost("/v1/mgmt/family/settings", SETTINGS_JSON);
    assertSettings(settings);
  }

  @Test
  void testConfigureSettingsPartial() throws Exception {
    respondToPost(SETTINGS_JSON);
    call(() -> familyService.configureSettings(FamilySettings.builder().enabled(true).build()));
    assertPost("/v1/mgmt/family/settings", "{\"enabled\":true}");
  }

  @Test
  void testConfigureSettingsForNullSettings() {
    assertInvalid("settings", () -> familyService.configureSettings(null));
  }

  private void respondToPost(String json) {
    doAnswer(inv -> parse(json, inv.getArgument(2))).when(apiProxy).post(any(), any(), any());
  }

  private static Object parse(String json, Class<?> clz) throws Exception {
    return MAPPER.readValue(json, clz);
  }

  @SuppressWarnings("unchecked")
  private <T> T call(Supplier<T> call) {
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      return call.get();
    }
  }

  private void assertPost(String path, String expectedBody) throws Exception {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), any());
    assertThat(uri.getValue().getPath()).isEqualTo(path);
    JsonNode sent = MAPPER.readTree(MAPPER.writeValueAsString(body.getValue()));
    assertThat(sent).isEqualTo(MAPPER.readTree(expectedBody));
    assertAuthHeader();
  }

  private void assertAuthHeader() {
    assertThat(authHeader).isEqualTo("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY);
  }

  private void assertInvalid(String argument, Runnable request) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> call(() -> {
      request.run();
      return null;
    }));
    assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
    verify(apiProxy, never()).post(any(), any(), any());
  }

  private static void assertFamily(Family family) {
    assertThat(family.getId()).isEqualTo("F1");
    assertThat(family.getName()).isEqualTo("fam");
    assertThat(family.getCustomAttributes()).containsEntry("a", "b");
    assertThat(family.getDisabled()).isTrue();
    assertThat(family.getPhoto()).isEqualTo("https://p");
    assertThat(family.getCreatedTime()).isEqualTo(123L);
  }

  private static void assertSettings(FamilySettings settings) {
    assertThat(settings.getEnabled()).isTrue();
    assertThat(settings.getMaxFamilyMembers()).isEqualTo(5);
    assertThat(settings.getAllowMultipleFamiliesUsers()).isFalse();
  }
}
