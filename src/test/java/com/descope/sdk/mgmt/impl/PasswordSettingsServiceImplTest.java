package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.passwordsettings.PasswordSettings;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.PasswordSettingsService;
import com.descope.sdk.mgmt.TenantService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class PasswordSettingsServiceImplTest {

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

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, Arrays.asList(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    passwordSettingsService.configureSettings(tenantId, mockSettings);
    PasswordSettings ps = passwordSettingsService.getSettings(tenantId);
    assertThat(ps).isEqualTo(mockSettings);
    tenantService.delete(tenantId);
    ps = passwordSettingsService.getSettings();
    assertThat(ps).isNotNull();
  }
}
