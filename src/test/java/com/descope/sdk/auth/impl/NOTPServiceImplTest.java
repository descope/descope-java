package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.descope.enums.NOTPProvider;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPRequestOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPTemplates;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.notp.request.NOTPSessionRequest;
import com.descope.model.notp.request.NOTPSignInRequest;
import com.descope.model.notp.request.NOTPSignUpRequest;
import com.descope.model.notp.request.NOTPUpdateRequest;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class NOTPServiceImplTest {
  private static final NOTPResponse MOCK_NOTP_RESPONSE =
      new NOTPResponse("somePendingRef", MOCK_URL, "someImage");
  private static final NOTPTemplates TEMPLATES = NOTPTemplates.builder()
      .verifyTemplateId("verify").successTemplateId("success").errorTemplateId("error").build();
  private static final NOTPRequestOptions REQUEST_OPTIONS = NOTPRequestOptions.builder()
      .providerId("someProviderId").templates(TEMPLATES).ssoAppId("someSsoAppId").build();

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
  private MockedStatic<ApiProxyBuilder> mockProxy() {
    MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class);
    mocked.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
    return mocked;
  }

  private <T> T captureBody(String path, Class<T> bodyClass, Class<?> responseClass) {
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<T> bodyCaptor = ArgumentCaptor.forClass(bodyClass);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(responseClass));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo(path);
    return bodyCaptor.getValue();
  }

  private static void assertResponse(NOTPResponse response) {
    assertThat(response.getPendingRef()).isEqualTo("somePendingRef");
    assertThat(response.getRedirectUrl()).isEqualTo(MOCK_URL);
    assertThat(response.getImage()).isEqualTo("someImage");
  }

  @Test
  void signUpSendsFullRequest() {
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    User user = User.builder().name(MOCK_NAME).email(MOCK_EMAIL).build();
    NOTPSignUpOptions signUpOptions = NOTPSignUpOptions.builder()
        .customClaims(Collections.singletonMap("k1", "v1"))
        .templateOptions(Collections.singletonMap("t1", "v1"))
        .templateId("someTemplateId")
        .locale("en")
        .tenantId("someTenant")
        .build();
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      NOTPResponse response =
          notpService.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, user, signUpOptions, REQUEST_OPTIONS);

      NOTPSignUpRequest sent =
          captureBody("/v1/auth/notp/whatsapp/signup", NOTPSignUpRequest.class, NOTPResponse.class);
      assertThat(sent.getLoginId()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getUser().getName()).isEqualTo(MOCK_NAME);
      assertThat(sent.getUser().getEmail()).isEqualTo(MOCK_EMAIL);
      assertThat(sent.getUser().getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getLoginOptions()).isEqualTo(signUpOptions);
      assertThat(sent.getProviderId()).isEqualTo("someProviderId");
      assertThat(sent.getTemplates()).isEqualTo(TEMPLATES);
      assertThat(sent.getSsoAppId()).isEqualTo("someSsoAppId");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertResponse(response);
    }
  }

  @Test
  void signUpOmitsUnsetFields() throws Exception {
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      notpService.signUp(NOTPProvider.WHATSAPP, null, null, null, null);

      NOTPSignUpRequest sent =
          captureBody("/v1/auth/notp/whatsapp/signup", NOTPSignUpRequest.class, NOTPResponse.class);
      assertThat(new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(sent))
          .isEqualTo("{\"user\":{}}");
    }
  }

  @Test
  void signInSendsFullRequest() {
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder()
        .stepup(true)
        .mfa(false)
        .customClaims(Collections.singletonMap("k1", "v1"))
        .templateOptions(Collections.singletonMap("t1", "v1"))
        .templateId("someTemplateId")
        .revokeOtherSessions(true)
        .revokeOtherSessionsTypes(new String[] {"web"})
        .locale("en")
        .tenantId("someTenant")
        .ssoAppId("someSsoAppId")
        .build();
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      NOTPResponse response = notpService.signIn(
          NOTPProvider.WHATSAPP, MOCK_PHONE, MOCK_REFRESH_TOKEN, loginOptions, REQUEST_OPTIONS);

      NOTPSignInRequest sent =
          captureBody("/v1/auth/notp/whatsapp/signin", NOTPSignInRequest.class, NOTPResponse.class);
      assertThat(sent.getLoginId()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getLoginOptions()).isEqualTo(loginOptions);
      assertThat(sent.getProviderId()).isEqualTo("someProviderId");
      assertThat(sent.getTemplates()).isEqualTo(TEMPLATES);
      assertThat(sent.getSsoAppId()).isEqualTo("someSsoAppId");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
      assertResponse(response);
    }
  }

  @Test
  void signInWithoutStepUpUsesProjectOnly() throws Exception {
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      notpService.signIn(NOTPProvider.WHATSAPP, null, MOCK_REFRESH_TOKEN, null, null);

      NOTPSignInRequest sent =
          captureBody("/v1/auth/notp/whatsapp/signin", NOTPSignInRequest.class, NOTPResponse.class);
      assertThat(sent.getLoginId()).isNull();
      assertThat(sent.getLoginOptions()).isNull();
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    }
  }

  @Test
  void signInStepUpRequiresRefreshToken() {
    NOTPLoginOptions loginOptions = NOTPLoginOptions.builder().mfa(true).build();
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> notpService.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, "", loginOptions, null));
      assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
      verifyNoInteractions(apiProxy);
    }
  }

  @Test
  void signUpOrInSendsFullRequest() {
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    Map<String, Object> customClaims = Collections.singletonMap("k1", "v1");
    Map<String, String> templateOptions = Collections.singletonMap("t1", "v1");
    NOTPSignUpOptions signUpOptions = NOTPSignUpOptions.builder()
        .customClaims(customClaims)
        .templateOptions(templateOptions)
        .templateId("someTemplateId")
        .locale("en")
        .tenantId("someTenant")
        .build();
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      NOTPResponse response =
          notpService.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE, signUpOptions, REQUEST_OPTIONS);

      NOTPSignInRequest sent =
          captureBody("/v1/auth/notp/whatsapp/signup-in", NOTPSignInRequest.class, NOTPResponse.class);
      assertThat(sent.getLoginId()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getLoginOptions()).isEqualTo(NOTPLoginOptions.builder()
          .customClaims(customClaims)
          .templateOptions(templateOptions)
          .templateId("someTemplateId")
          .locale("en")
          .tenantId("someTenant")
          .build());
      assertThat(sent.getProviderId()).isEqualTo("someProviderId");
      assertThat(sent.getTemplates()).isEqualTo(TEMPLATES);
      assertThat(sent.getSsoAppId()).isEqualTo("someSsoAppId");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertResponse(response);
    }
  }

  @Test
  void updateUserSendsFullRequest() {
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    NOTPUpdateOptions updateOptions = NOTPUpdateOptions.builder()
        .addToLoginIds(true)
        .onMergeUseExisting(true)
        .failOnConflict(true)
        .providerId("someProviderId")
        .templates(TEMPLATES)
        .templateOptions(Collections.singletonMap("t1", "v1"))
        .ssoAppId("someSsoAppId")
        .locale("en")
        .build();
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      NOTPResponse response = notpService.updateUser(
          NOTPProvider.WHATSAPP, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, updateOptions);

      NOTPUpdateRequest sent =
          captureBody("/v1/auth/notp/whatsapp/update", NOTPUpdateRequest.class, NOTPResponse.class);
      assertThat(sent).isEqualTo(NOTPUpdateRequest.builder()
          .loginId(MOCK_EMAIL)
          .phone(MOCK_PHONE)
          .addToLoginIds(true)
          .onMergeUseExisting(true)
          .failOnConflict(true)
          .providerId("someProviderId")
          .templates(TEMPLATES)
          .templateOptions(Collections.singletonMap("t1", "v1"))
          .ssoAppId("someSsoAppId")
          .locale("en")
          .build());
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
      assertResponse(response);
    }
  }

  @Test
  void updateUserSendsAddToLoginIdsKey() throws Exception {
    String json = new ObjectMapper().writeValueAsString(NOTPUpdateRequest.builder().addToLoginIds(true).build());
    assertThat(json).contains("\"addToLoginIDs\":true");
  }

  @Test
  void updateUserRejectsInvalidArguments() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
          () -> notpService.updateUser(NOTPProvider.WHATSAPP, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null))
          .getMessage());
      assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
          () -> notpService.updateUser(NOTPProvider.WHATSAPP, MOCK_EMAIL, MOCK_PHONE, "", null))
          .getMessage());
      verifyNoInteractions(apiProxy);
    }
  }

  @Test
  void rejectsMissingProvider() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
          () -> notpService.signUp(null, MOCK_PHONE, null, null, null)).getMessage());
      assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
          () -> notpService.signIn(null, MOCK_PHONE, null, null, null)).getMessage());
      assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
          () -> notpService.signUpOrIn(null, MOCK_PHONE, null, null)).getMessage());
      assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
          () -> notpService.updateUser(null, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null)).getMessage());
      verifyNoInteractions(apiProxy);
    }
  }

  @Test
  void getSessionSendsPendingRef() {
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = notpService.getSession("somePendingRef");
      }
      NOTPSessionRequest sent =
          captureBody("/v1/auth/notp/pending-session", NOTPSessionRequest.class, JWTResponse.class);
      assertThat(sent.getPendingRef()).isEqualTo("somePendingRef");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    }
    assertThat(authenticationInfo.getToken().getJwt()).isNotBlank();
    assertThat(authenticationInfo.getRefreshToken().getJwt()).isNotBlank();
    assertThat(authenticationInfo.getUser().getUserId()).isNotBlank();
  }
}
