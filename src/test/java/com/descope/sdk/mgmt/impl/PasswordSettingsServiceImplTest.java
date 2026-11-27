package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.passwordsettings.PasswordSettings;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.PasswordSettingsService;
import com.descope.sdk.mgmt.TenantService;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class PasswordSettingsServiceImplTest {

  private static final String MGMT_PROJECT_ID = "P123456789012345678901234567";
  private static final String MGMT_KEY = "some-management-key";

  private final PasswordSettings mockSettings = PasswordSettings.builder()
      .enabled(true)
      .expiration(true)
      .expirationWeeks(2)
      .lock(true)
      .lockAttempts(3)
      .lowercase(true)
      .minLength(10)
      .nonAlphanumeric(true)
      .number(true)
      .reuse(true)
      .reuseAmount(10)
      .uppercase(true)
      .build();
  private final PasswordSettings mockFullSettings = PasswordSettings.builder()
      .enabled(true)
      .minLength(10)
      .lowercase(true)
      .uppercase(true)
      .number(true)
      .nonAlphanumeric(true)
      .expiration(true)
      .expirationWeeks(2)
      .reuse(true)
      .reuseAmount(10)
      .lock(true)
      .lockAttempts(3)
      .tenantId("someTenantId")
      .tempLock(true)
      .tempLockAttempts(5)
      .tempLockDuration(15)
      .enablePasswordStrength(true)
      .passwordStrengthScore(3)
      .anyLetter(true)
      .disallowedCharacters("<>")
      .disallowEmailMatch(true)
      .build();
  private PasswordSettingsService passwordSettingsService;
  private TenantService tenantService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmt = ManagementServiceBuilder.buildServices(client);
    this.passwordSettingsService = mgmt.getPasswordSettingsService();
    this.tenantService = mgmt.getTenantService();
  }

  @Test
  void testGetSettingsForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> passwordSettingsService.getSettings(""));
    assertNotNull(thrown);
    assertEquals("The tenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetSettingsProjectForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSettings).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      PasswordSettings response = passwordSettingsService.getSettings();
      assertThat(response).isEqualTo(mockSettings);
    }
  }

  @Test
  void testGetSettingsTenantForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSettings).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      PasswordSettings response = passwordSettingsService.getSettings("a");
      assertThat(response).isEqualTo(mockSettings);
    }
  }

  @Test
  void testConfigureSettingsForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> passwordSettingsService.configureSettings("", null));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForNoSettings() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> passwordSettingsService.configureSettings("a", null));
    assertNotNull(thrown);
    assertEquals("The settings argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForNoSettingsProject() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> passwordSettingsService.configureSettings(null));
    assertNotNull(thrown);
    assertEquals("The settings argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetSettingsTenantWithAllFieldsForSuccess() {
    PasswordSettingsService service = buildServiceWithManagementKey();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockFullSettings).when(apiProxy).get(any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      PasswordSettings response = service.getSettings("someTenantId");
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy, times(1)).get(uriCaptor.capture(), any());
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/mgmt/password/settings");
      assertThat(uriCaptor.getValue().getQuery()).isEqualTo("tenantId=someTenantId");
      assertThat(authHeader.get()).isEqualTo("Bearer " + MGMT_PROJECT_ID + ":" + MGMT_KEY);
      assertThat(response).isEqualTo(mockFullSettings);
      assertThat(response.getTenantId()).isEqualTo("someTenantId");
      assertThat(response.getTempLock()).isTrue();
      assertThat(response.getTempLockAttempts()).isEqualTo(5);
      assertThat(response.getTempLockDuration()).isEqualTo(15);
      assertThat(response.getEnablePasswordStrength()).isTrue();
      assertThat(response.getPasswordStrengthScore()).isEqualTo(3);
      assertThat(response.getAnyLetter()).isTrue();
      assertThat(response.getDisallowedCharacters()).isEqualTo("<>");
      assertThat(response.getDisallowEmailMatch()).isTrue();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testConfigureSettingsTenantWithAllFieldsForSuccess() {
    PasswordSettingsService service = buildServiceWithManagementKey();
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      service.configureSettings("someTenantId", mockFullSettings);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), any());
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/mgmt/password/settings");
      assertThat(uriCaptor.getValue().getQuery()).isNull();
      assertThat(authHeader.get()).isEqualTo("Bearer " + MGMT_PROJECT_ID + ":" + MGMT_KEY);
      Map<String, Object> body = bodyCaptor.getValue();
      assertThat(body).hasSize(21);
      assertThat(body).containsEntry("tenantId", "someTenantId");
      assertThat(body).containsEntry("enabled", true);
      assertThat(body).containsEntry("minLength", 10);
      assertThat(body).containsEntry("lowercase", true);
      assertThat(body).containsEntry("uppercase", true);
      assertThat(body).containsEntry("number", true);
      assertThat(body).containsEntry("nonAlphanumeric", true);
      assertThat(body).containsEntry("expiration", true);
      assertThat(body).containsEntry("expirationWeeks", 2);
      assertThat(body).containsEntry("reuse", true);
      assertThat(body).containsEntry("reuseAmount", 10);
      assertThat(body).containsEntry("lock", true);
      assertThat(body).containsEntry("lockAttempts", 3);
      assertThat(body).containsEntry("tempLock", true);
      assertThat(body).containsEntry("tempLockAttempts", 5);
      assertThat(body).containsEntry("tempLockDuration", 15);
      assertThat(body).containsEntry("enablePasswordStrength", true);
      assertThat(body).containsEntry("passwordStrengthScore", 3);
      assertThat(body).containsEntry("anyLetter", true);
      assertThat(body).containsEntry("disallowedCharacters", "<>");
      assertThat(body).containsEntry("disallowEmailMatch", true);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testConfigureSettingsProjectOmitsUnsetFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      passwordSettingsService.configureSettings(mockSettings);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy, times(1)).post(any(), bodyCaptor.capture(), any());
      assertThat(bodyCaptor.getValue()).hasSize(12).doesNotContainKeys("tenantId", "tempLock", "tempLockAttempts",
          "tempLockDuration", "enablePasswordStrength", "passwordStrengthScore", "anyLetter", "disallowedCharacters",
          "disallowEmailMatch");
    }
  }

  @Test
  void testLegacyConstructorLeavesNewFieldsUnset() {
    PasswordSettings settings = new PasswordSettings(true, 10, true, true, true, true, true, 2, true, 10, true, 3);
    assertThat(settings.getLockAttempts()).isEqualTo(3);
    assertThat(settings.getTempLock()).isNull();
    assertThat(settings.getDisallowedCharacters()).isNull();
  }

  private static PasswordSettingsService buildServiceWithManagementKey() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(MGMT_PROJECT_ID)
        .managementKey(MGMT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return ManagementServiceBuilder.buildServices(client).getPasswordSettingsService();
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, Arrays.asList(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    try {
      passwordSettingsService.configureSettings(tenantId, mockSettings);
      PasswordSettings ps = passwordSettingsService.getSettings(tenantId);
      assertThat(ps).usingRecursiveComparison().ignoringFields("tenantId", "tempLock", "tempLockAttempts",
          "tempLockDuration", "enablePasswordStrength", "passwordStrengthScore", "anyLetter", "disallowedCharacters",
          "disallowEmailMatch").isEqualTo(mockSettings);
    } finally {
      tenantService.delete(tenantId);
    }
    PasswordSettings ps = passwordSettingsService.getSettings();
    assertThat(ps).isNotNull();
  }
}
