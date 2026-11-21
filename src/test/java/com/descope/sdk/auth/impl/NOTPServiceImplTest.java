package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
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
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class NOTPServiceImplTest {
  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final NOTPResponse MOCK_RESPONSE =
      new NOTPResponse("somePendingRef", "https://wa.me/1234567890?text=code", "someImage");
  private static final NOTPOptions OPTIONS = NOTPOptions.builder()
      .providerId("someProviderId")
      .templates(NOTPTemplateIds.builder()
          .verifyTemplateId("v1").successTemplateId("s1").errorTemplateId("e1").build())
      .ssoAppId("someSsoAppId")
      .build();
  private static final String OPTIONS_JSON = "\"providerId\":\"someProviderId\","
      + "\"templates\":{\"verifyTemplateId\":\"v1\",\"successTemplateId\":\"s1\",\"errorTemplateId\":\"e1\"},"
      + "\"ssoAppId\":\"someSsoAppId\"";

  private NOTPService notpService;
  private ApiProxy apiProxy;
  private AtomicReference<String> authHeader;

  @BeforeEach
  void setUp() {
    this.notpService = AuthenticationServiceBuilder.buildServices(TestUtils.getClient()).getNotpService();
    this.apiProxy = mock(ApiProxy.class);
    this.authHeader = new AtomicReference<>();
  }

  @SuppressWarnings("unchecked")
  private <T> T call(Supplier<T> call) {
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      return call.get();
    }
  }

  @SneakyThrows
  private void verifyPost(String path, String expectedBody, Class<?> responseClass) {
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(responseClass));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo(path);
    JsonNode actual = MAPPER.readTree(MAPPER.writeValueAsString(bodyCaptor.getValue()));
    assertThat(actual).isEqualTo(MAPPER.readTree(expectedBody));
  }

  @Test
  void testSignIn() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder()
        .customClaims(mapOf("k1", "v1"))
        .templateOptions(mapOf("t1", "v1"))
        .locale("en")
        .templateId("someTemplateId")
        .tenantId("someTenant")
        .revokeOtherSessions(true)
        .revokeOtherSessionsTypes(Arrays.asList("web"))
        .build();
    NOTPResponse response =
        call(() -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, OPTIONS, null));
    assertThat(response).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    verifyPost("/v1/auth/notp/whatsapp/signin", "{\"provider\":\"whatsapp\",\"loginId\":\"" + MOCK_PHONE + "\","
        + "\"loginOptions\":{\"customClaims\":{\"k1\":\"v1\"},\"templateOptions\":{\"t1\":\"v1\"},\"locale\":\"en\","
        + "\"templateId\":\"someTemplateId\",\"revokeOtherSessions\":true,\"revokeOtherSessionsTypes\":[\"web\"],"
        + "\"tenantId\":\"someTenant\"}," + OPTIONS_JSON + "}", NOTPResponse.class);
  }

  @Test
  void testSignInWithEmptyLoginId() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    call(() -> notpService.signIn(NOTPProvider.WHATSAPP, "", null, null, null));
    verifyPost("/v1/auth/notp/whatsapp/signin", "{\"provider\":\"whatsapp\"}", NOTPResponse.class);
  }

  @Test
  void testSignInStepUp() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().stepup(true).build();
    call(() -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, null, MOCK_REFRESH_TOKEN));
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    verifyPost("/v1/auth/notp/whatsapp/signin", "{\"provider\":\"whatsapp\",\"loginId\":\"" + MOCK_PHONE + "\","
        + "\"loginOptions\":{\"stepup\":true}}", NOTPResponse.class);
  }

  @Test
  void testSignInStepUpWithoutRefreshToken() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().mfa(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, null, ""));
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
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    NOTPSignUpUser user = NOTPSignUpUser.builder()
        .loginId("someLoginId")
        .name("someName")
        .email("someone@example.com")
        .givenName("given")
        .middleName("middle")
        .familyName("family")
        .recoveryEmail("recovery@example.com")
        .recoveryPhone("+15555555556")
        .build();
    NOTPSignUpOptions signUpOptions = NOTPSignUpOptions.builder()
        .customClaims(mapOf("aa", "bb"))
        .templateOptions(mapOf("cc", "dd"))
        .locale("en")
        .templateId("someTemplateId")
        .tenantId("someTenant")
        .build();
    NOTPResponse response =
        call(() -> notpService.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, user, signUpOptions, OPTIONS));
    assertThat(response).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    verifyPost("/v1/auth/notp/whatsapp/signup", "{\"provider\":\"whatsapp\",\"phone\":\"" + MOCK_PHONE + "\","
        + "\"loginId\":\"" + MOCK_PHONE + "\",\"user\":{\"loginId\":\"someLoginId\",\"name\":\"someName\","
        + "\"phone\":\"" + MOCK_PHONE + "\",\"email\":\"someone@example.com\",\"givenName\":\"given\","
        + "\"middleName\":\"middle\",\"familyName\":\"family\",\"recoveryEmail\":\"recovery@example.com\","
        + "\"recoveryPhone\":\"+15555555556\"}," + OPTIONS_JSON + ",\"loginOptions\":{\"customClaims\":{\"aa\":\"bb\"},"
        + "\"templateOptions\":{\"cc\":\"dd\"},\"locale\":\"en\",\"templateId\":\"someTemplateId\","
        + "\"tenantId\":\"someTenant\"}}", NOTPResponse.class);
  }

  @Test
  void testSignUpWithoutUserOrLoginId() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    call(() -> notpService.signUp(NOTPProvider.WHATSAPP, null, null, null, null));
    verifyPost("/v1/auth/notp/whatsapp/signup", "{\"provider\":\"whatsapp\",\"user\":{}}", NOTPResponse.class);
  }

  @Test
  void testSignUpWithoutProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signUp(null, MOCK_PHONE, null, null, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrIn() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().customClaims(mapOf("aa", "bb")).build();
    NOTPResponse response =
        call(() -> notpService.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, OPTIONS, null));
    assertThat(response).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    verifyPost("/v1/auth/notp/whatsapp/signup-in", "{\"provider\":\"whatsapp\",\"loginId\":\"" + MOCK_PHONE + "\","
        + "\"loginOptions\":{\"customClaims\":{\"aa\":\"bb\"}}," + OPTIONS_JSON + "}", NOTPResponse.class);
  }

  @Test
  void testSignUpOrInStepUp() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().mfa(true).build();
    call(() -> notpService.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, null, MOCK_REFRESH_TOKEN));
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
  }

  @Test
  void testSignUpOrInStepUpWithoutRefreshToken() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().stepup(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, null, null));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInWithoutProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> notpService.signUpOrIn(null, MOCK_PHONE, null, null, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhone() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    NOTPUpdateOptions updateOptions = NOTPUpdateOptions.builder()
        .addToLoginIds(true)
        .onMergeUseExisting(true)
        .failOnConflict(true)
        .templateOptions(mapOf("cc", "dd"))
        .locale("en")
        .providerId("someProviderId")
        .templates(OPTIONS.getTemplates())
        .ssoAppId("someSsoAppId")
        .build();
    NOTPResponse response = call(() -> notpService.updateUserPhone(
        NOTPProvider.WHATSAPP, "someLoginId", MOCK_PHONE, MOCK_REFRESH_TOKEN, updateOptions));
    assertThat(response).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    verifyPost("/v1/auth/notp/whatsapp/update", "{\"provider\":\"whatsapp\",\"loginId\":\"someLoginId\","
        + "\"phone\":\"" + MOCK_PHONE + "\",\"addToLoginIDs\":true,\"onMergeUseExisting\":true,"
        + "\"templateOptions\":{\"cc\":\"dd\"},\"locale\":\"en\",\"failOnConflict\":true," + OPTIONS_JSON + "}",
        NOTPResponse.class);
  }

  @Test
  void testUpdateUserPhoneWithoutOptions() {
    doReturn(MOCK_RESPONSE).when(apiProxy).post(any(), any(), eq(NOTPResponse.class));
    call(() -> notpService.updateUserPhone(NOTPProvider.WHATSAPP, "someLoginId", "", MOCK_REFRESH_TOKEN, null));
    verifyPost("/v1/auth/notp/whatsapp/update", "{\"provider\":\"whatsapp\",\"loginId\":\"someLoginId\"}",
        NOTPResponse.class);
  }

  @Test
  void testUpdateUserPhoneInvalidArguments() {
    assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.updateUserPhone(null, "someLoginId", MOCK_PHONE, MOCK_REFRESH_TOKEN, null))
        .getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.updateUserPhone(NOTPProvider.WHATSAPP, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null))
        .getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.updateUserPhone(NOTPProvider.WHATSAPP, "someLoginId", MOCK_PHONE, "", null))
        .getMessage());
  }

  @Test
  void testGetSession() {
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), eq(JWTResponse.class));
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    AuthenticationInfo info = call(() -> {
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        return notpService.getSession("somePendingRef");
      }
    });
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    verifyPost("/v1/auth/notp/pending-session", "{\"pendingRef\":\"somePendingRef\"}", JWTResponse.class);
    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getUser()).isEqualTo(MOCK_JWT_RESPONSE.getUser());
    assertThat(info.getFirstSeen()).isEqualTo(MOCK_JWT_RESPONSE.getFirstSeen());
  }

  @Test
  void testGetSessionWithoutPendingRef() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> notpService.getSession(""));
    assertEquals("The Pending Ref argument is invalid", thrown.getMessage());
  }
}
