package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.NOTPProvider;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPSignUpUser;
import com.descope.model.notp.NOTPTemplateIds;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class NOTPServiceImplTest {
  private static final NOTPResponse MOCK_NOTP_RESPONSE =
      new NOTPResponse("somePendingRef", "https://wa.me/1234567890?text=code", "someImage");
  private static final NOTPTemplateIds TEMPLATES = NOTPTemplateIds.builder()
      .verifyTemplateId("verifyT").successTemplateId("successT").errorTemplateId("errorT").build();
  private static final NOTPOptions OPTIONS =
      NOTPOptions.builder().providerId("someProviderId").templates(TEMPLATES).ssoAppId("someSsoApp").build();

  private NOTPService notpService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.notpService = AuthenticationServiceBuilder.buildServices(client).getNotpService();
  }

  @Test
  void testSignIn() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder()
        .customClaims(Collections.singletonMap("k1", "v1"))
        .templateOptions(Collections.singletonMap("t1", "v1"))
        .locale("en")
        .templateId("someTemplate")
        .revokeOtherSessions(true)
        .revokeOtherSessionsTypes(Arrays.asList("web"))
        .tenantId("someTenant")
        .ssoAppId("someSsoApp")
        .build();
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE,
        () -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, null, OPTIONS));

    assertThat(call.result).isEqualTo(MOCK_NOTP_RESPONSE);
    assertThat(call.uri.getPath()).isEqualTo("/v1/auth/notp/whatsapp/signin");
    assertThat(call.authHeader).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = call.body();
    assertThat(body).containsEntry("provider", "whatsapp").containsEntry("loginId", MOCK_PHONE)
        .containsEntry("providerId", "someProviderId").containsEntry("ssoAppId", "someSsoApp");
    assertThat(body.get("templates")).isEqualTo(templatesMap());
    @SuppressWarnings("unchecked")
    Map<String, Object> sentOptions = (Map<String, Object>) body.get("loginOptions");
    assertThat(sentOptions).containsEntry("stepup", false).containsEntry("mfa", false)
        .containsEntry("customClaims", Collections.singletonMap("k1", "v1"))
        .containsEntry("templateOptions", Collections.singletonMap("t1", "v1"))
        .containsEntry("locale", "en").containsEntry("templateId", "someTemplate")
        .containsEntry("revokeOtherSessions", true)
        .containsEntry("revokeOtherSessionsTypes", Arrays.asList("web"))
        .containsEntry("tenantId", "someTenant").containsEntry("ssoAppId", "someSsoApp");
  }

  @Test
  void testSignInWithoutLoginId() {
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE,
        () -> notpService.signIn(NOTPProvider.WHATSAPP, "", null, null, null));

    assertThat(call.body()).containsOnlyKeys("provider");
  }

  @Test
  void testSignInStepUpSendsRefreshToken() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().stepup(true).build();
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE,
        () -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, MOCK_REFRESH_TOKEN, null));

    assertThat(call.authHeader).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
  }

  @Test
  void testSignInStepUpWithoutRefreshToken() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().mfa(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, "", null));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithoutProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signIn(null, MOCK_PHONE, null, null, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUp() {
    NOTPSignUpUser user = NOTPSignUpUser.builder().name(MOCK_NAME).recoveryEmail("r@domain.com").build();
    NOTPSignUpOptions signUpOptions = NOTPSignUpOptions.builder()
        .customClaims(Collections.singletonMap("aa", "bb"))
        .templateOptions(Collections.singletonMap("cc", "dd"))
        .locale("en")
        .templateId("someTemplate")
        .tenantId("someTenant")
        .build();
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE,
        () -> notpService.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, user, signUpOptions, OPTIONS));

    assertThat(call.result).isEqualTo(MOCK_NOTP_RESPONSE);
    assertThat(call.uri.getPath()).isEqualTo("/v1/auth/notp/whatsapp/signup");
    assertThat(call.authHeader).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = call.body();
    assertThat(body).containsEntry("provider", "whatsapp").containsEntry("loginId", MOCK_PHONE)
        .containsEntry("phone", MOCK_PHONE).containsEntry("providerId", "someProviderId")
        .containsEntry("ssoAppId", "someSsoApp").containsEntry("templates", templatesMap());
    assertThat(body.get("user")).isEqualTo(mapOf("name", MOCK_NAME, "phone", MOCK_PHONE,
        "recoveryEmail", "r@domain.com"));
    assertThat(body.get("loginOptions")).isEqualTo(mapOf("customClaims", Collections.singletonMap("aa", "bb"),
        "templateOptions", Collections.singletonMap("cc", "dd"), "locale", "en", "templateId", "someTemplate",
        "tenantId", "someTenant"));
  }

  @Test
  void testSignUpWithoutUser() {
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE,
        () -> notpService.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, null, null, null));

    assertThat(call.body()).containsOnlyKeys("provider", "loginId", "phone", "user");
    assertThat(call.body().get("user")).isEqualTo(Collections.singletonMap("phone", MOCK_PHONE));
  }

  @Test
  void testSignUpWithoutProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signUp(null, MOCK_PHONE, null, null, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrIn() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder()
        .customClaims(Collections.singletonMap("aa", "bb")).tenantId("someTenant").build();
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE,
        () -> notpService.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, OPTIONS));

    assertThat(call.result).isEqualTo(MOCK_NOTP_RESPONSE);
    assertThat(call.uri.getPath()).isEqualTo("/v1/auth/notp/whatsapp/signup-in");
    assertThat(call.authHeader).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = call.body();
    assertThat(body).containsEntry("provider", "whatsapp").containsEntry("loginId", MOCK_PHONE)
        .containsEntry("providerId", "someProviderId").containsEntry("ssoAppId", "someSsoApp")
        .containsEntry("templates", templatesMap());
    @SuppressWarnings("unchecked")
    Map<String, Object> sentOptions = (Map<String, Object>) body.get("loginOptions");
    assertThat(sentOptions).containsEntry("customClaims", Collections.singletonMap("aa", "bb"))
        .containsEntry("tenantId", "someTenant");
  }

  @Test
  void testSignUpOrInWithoutProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signUpOrIn(null, MOCK_PHONE, null, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetSession() {
    Call call;
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      call = run(JWTResponse.class, MOCK_JWT_RESPONSE, () -> notpService.getSession("somePendingRef"));
    }

    AuthenticationInfo info = (AuthenticationInfo) call.result;
    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getUser()).isEqualTo(MOCK_JWT_RESPONSE.getUser());
    assertThat(info.getFirstSeen()).isTrue();
    assertThat(call.uri.getPath()).isEqualTo("/v1/auth/notp/pending-session");
    assertThat(call.authHeader).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(call.body()).isEqualTo(Collections.singletonMap("pendingRef", "somePendingRef"));
  }

  @Test
  void testUpdateUserPhone() {
    NOTPUpdateOptions updateOptions = NOTPUpdateOptions.builder()
        .addToLoginIds(true)
        .onMergeUseExisting(true)
        .providerId("someProviderId")
        .templates(TEMPLATES)
        .templateOptions(Collections.singletonMap("cc", "dd"))
        .ssoAppId("someSsoApp")
        .locale("en")
        .failOnConflict(true)
        .build();
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE, () -> notpService.updateUserPhone(
        NOTPProvider.WHATSAPP, "someLoginId", MOCK_PHONE, MOCK_REFRESH_TOKEN, updateOptions));

    assertThat(call.result).isEqualTo(MOCK_NOTP_RESPONSE);
    assertThat(call.uri.getPath()).isEqualTo("/v1/auth/notp/whatsapp/update");
    assertThat(call.authHeader).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(call.body()).isEqualTo(mapOf("provider", "whatsapp", "loginId", "someLoginId", "phone", MOCK_PHONE,
        "addToLoginIDs", true, "onMergeUseExisting", true, "providerId", "someProviderId",
        "templates", templatesMap(), "templateOptions", Collections.singletonMap("cc", "dd"),
        "ssoAppId", "someSsoApp", "locale", "en", "failOnConflict", true));
  }

  @Test
  void testUpdateUserPhoneWithoutOptions() {
    Call call = run(NOTPResponse.class, MOCK_NOTP_RESPONSE, () -> notpService.updateUserPhone(
        NOTPProvider.WHATSAPP, "someLoginId", null, MOCK_REFRESH_TOKEN, null));

    assertThat(call.body()).isEqualTo(mapOf("provider", "whatsapp", "loginId", "someLoginId",
        "addToLoginIDs", false, "onMergeUseExisting", false, "failOnConflict", false));
  }

  @Test
  void testUpdateUserPhoneWithoutLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.updateUserPhone(NOTPProvider.WHATSAPP, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneWithoutRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.updateUserPhone(NOTPProvider.WHATSAPP, "someLoginId", MOCK_PHONE, "", null));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneWithoutProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.updateUserPhone(null, "someLoginId", MOCK_PHONE, MOCK_REFRESH_TOKEN, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  private static Map<String, Object> templatesMap() {
    return mapOf("verifyTemplateId", "verifyT", "successTemplateId", "successT", "errorTemplateId", "errorT");
  }

  private static Map<String, Object> mapOf(Object... keyValues) {
    Map<String, Object> map = new HashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      map.put((String) keyValues[i], keyValues[i + 1]);
    }
    return map;
  }

  private static class Call {
    private Object result;
    private URI uri;
    private Object request;
    private String authHeader;

    private Map<String, Object> body() {
      ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
      return mapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
    }
  }

  /** Runs the call against a mocked proxy that answers with the response, capturing what was sent. */
  @SuppressWarnings("unchecked")
  private static <R> Call run(Class<R> responseClass, R response, Supplier<Object> action) {
    Call call = new Call();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), eq(responseClass));
    AtomicReference<String> header = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.result = action.get();
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(responseClass));
    call.uri = uriCaptor.getValue();
    call.request = bodyCaptor.getValue();
    call.authHeader = header.get();
    return call;
  }
}
