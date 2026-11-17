package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPRequestOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPTemplates;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class NOTPServiceImplTest {

  private static final String BASE_URL = "https://api.descope.com";
  private static final String PROVIDER = "whatsapp";
  private static final NOTPResponse MOCK_RESPONSE =
      new NOTPResponse("somePendingRef", "https://wa.me/1234567890", "someImage");
  private static final NOTPTemplates TEMPLATES = new NOTPTemplates("verify", "success", "error");
  private static final NOTPRequestOptions REQUEST_OPTIONS = new NOTPRequestOptions("providerId", TEMPLATES, "ssoApp");

  private NOTPService notpService;
  private ApiProxy apiProxy;
  private AtomicReference<String> authHeader;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri(BASE_URL)
        .projectId(PROJECT_ID)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    notpService = AuthenticationServiceBuilder.buildServices(client).getNotpService();
    apiProxy = mock(ApiProxy.class);
    authHeader = new AtomicReference<>();
  }

  @Test
  void testSignUp() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    User user = User.builder().name("name").givenName("given").email("a@b.com").build();
    NOTPSignUpOptions options = NOTPSignUpOptions.builder()
        .customClaims(mapOf("k1", "v1")).templateOptions(mapOf("t1", "v1")).locale("en").tenantId("t").build();
    NOTPResponse res = call(s -> s.signUp(PROVIDER, MOCK_PHONE, user, options, REQUEST_OPTIONS));
    assertThat(res).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(body("/v1/auth/notp/whatsapp/signup")).isEqualTo(json("{"
        + "\"loginId\":\"" + MOCK_PHONE + "\",\"phone\":\"" + MOCK_PHONE + "\","
        + "\"user\":{\"name\":\"name\",\"email\":\"a@b.com\",\"phone\":\"" + MOCK_PHONE + "\",\"givenName\":\"given\"},"
        + "\"loginOptions\":{\"customClaims\":{\"k1\":\"v1\"},\"templateOptions\":{\"t1\":\"v1\"},"
        + "\"locale\":\"en\",\"tenantId\":\"t\"},"
        + "\"providerId\":\"providerId\",\"ssoAppId\":\"ssoApp\","
        + "\"templates\":{\"verifyTemplateId\":\"verify\",\"successTemplateId\":\"success\","
        + "\"errorTemplateId\":\"error\"}}"));
  }

  @Test
  void testSignUpNoOptions() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    call(s -> s.signUp(PROVIDER, MOCK_PHONE, null, null, null));
    assertThat(body("/v1/auth/notp/whatsapp/signup")).isEqualTo(json("{"
        + "\"loginId\":\"" + MOCK_PHONE + "\",\"phone\":\"" + MOCK_PHONE + "\","
        + "\"user\":{\"phone\":\"" + MOCK_PHONE + "\"}}"));
  }

  @Test
  void testSignIn() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    NOTPLoginOptions options = NOTPLoginOptions.builder().customClaims(mapOf("k1", "v1")).tenantId("t").build();
    NOTPResponse res = call(s -> s.signIn(PROVIDER, MOCK_PHONE, null, options, REQUEST_OPTIONS));
    assertThat(res).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(body("/v1/auth/notp/whatsapp/signin")).isEqualTo(json("{"
        + "\"loginId\":\"" + MOCK_PHONE + "\","
        + "\"loginOptions\":{\"customClaims\":{\"k1\":\"v1\"},\"tenantId\":\"t\"},"
        + "\"providerId\":\"providerId\",\"ssoAppId\":\"ssoApp\","
        + "\"templates\":{\"verifyTemplateId\":\"verify\",\"successTemplateId\":\"success\","
        + "\"errorTemplateId\":\"error\"}}"));
  }

  @Test
  void testSignInStepup() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    NOTPLoginOptions options = NOTPLoginOptions.builder().stepup(true).build();
    call(s -> s.signIn(PROVIDER, null, MOCK_REFRESH_TOKEN, options, null));
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(body("/v1/auth/notp/whatsapp/signin")).isEqualTo(json("{\"loginOptions\":{\"stepup\":true}}"));
  }

  @Test
  void testSignInStepupNoRefreshToken() {
    NOTPLoginOptions options = NOTPLoginOptions.builder().mfa(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> call(s -> s.signIn(PROVIDER, MOCK_PHONE, "", options, null)));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrIn() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    NOTPSignUpOptions options = NOTPSignUpOptions.builder()
        .customClaims(mapOf("k1", "v1")).templateOptions(mapOf("t1", "v1")).locale("en").tenantId("t").build();
    NOTPResponse res = call(s -> s.signUpOrIn(PROVIDER, MOCK_PHONE, options, REQUEST_OPTIONS));
    assertThat(res).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(body("/v1/auth/notp/whatsapp/signup-in")).isEqualTo(json("{"
        + "\"loginId\":\"" + MOCK_PHONE + "\","
        + "\"loginOptions\":{\"customClaims\":{\"k1\":\"v1\"},\"templateOptions\":{\"t1\":\"v1\"},"
        + "\"locale\":\"en\",\"tenantId\":\"t\"},"
        + "\"providerId\":\"providerId\",\"ssoAppId\":\"ssoApp\","
        + "\"templates\":{\"verifyTemplateId\":\"verify\",\"successTemplateId\":\"success\","
        + "\"errorTemplateId\":\"error\"}}"));
  }

  @Test
  void testUpdateUser() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    NOTPUpdateOptions options = NOTPUpdateOptions.builder().addToLoginIds(true).onMergeUseExisting(true)
        .failOnConflict(true).providerId("providerId").templates(TEMPLATES).templateOptions(mapOf("t1", "v1"))
        .ssoAppId("ssoApp").locale("en").build();
    NOTPResponse res = call(s -> s.updateUser(PROVIDER, "loginId", MOCK_PHONE, MOCK_REFRESH_TOKEN, options));
    assertThat(res).isEqualTo(MOCK_RESPONSE);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(body("/v1/auth/notp/whatsapp/update")).isEqualTo(json("{"
        + "\"loginId\":\"loginId\",\"phone\":\"" + MOCK_PHONE + "\",\"addToLoginIDs\":true,"
        + "\"onMergeUseExisting\":true,\"failOnConflict\":true,\"providerId\":\"providerId\","
        + "\"templates\":{\"verifyTemplateId\":\"verify\",\"successTemplateId\":\"success\","
        + "\"errorTemplateId\":\"error\"},\"templateOptions\":{\"t1\":\"v1\"},\"ssoAppId\":\"ssoApp\","
        + "\"locale\":\"en\"}"));
  }

  @Test
  void testUpdateUserNoOptions() {
    when(apiProxy.post(any(), any(), eq(NOTPResponse.class))).thenReturn(MOCK_RESPONSE);
    call(s -> s.updateUser(PROVIDER, "loginId", null, MOCK_REFRESH_TOKEN, null));
    assertThat(body("/v1/auth/notp/whatsapp/update")).isEqualTo(json("{\"loginId\":\"loginId\"}"));
  }

  @Test
  void testUpdateUserEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> call(s -> s.updateUser(PROVIDER, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null)));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> call(s -> s.updateUser(PROVIDER, "loginId", MOCK_PHONE, "", null)));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testInvalidProvider() {
    for (String provider : new String[] {null, "", " ", "../mgmt", "whats/app", "a?b"}) {
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> call(s -> s.signUpOrIn(provider, MOCK_PHONE, null, null)));
      assertEquals("The Provider argument is invalid", thrown.getMessage());
    }
  }

  @Test
  void testGetSession() {
    when(apiProxy.post(any(), any(), eq(JWTResponse.class))).thenReturn(MOCK_JWT_RESPONSE);
    AuthenticationInfo info;
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      info = call(s -> s.getSession("somePendingRef"));
    }
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getUser()).isEqualTo(MOCK_JWT_RESPONSE.getUser());
    assertThat(info.getFirstSeen()).isEqualTo(MOCK_JWT_RESPONSE.getFirstSeen());
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JWTResponse.class));
    assertThat(uri.getValue().toString()).isEqualTo(BASE_URL + "/v1/auth/notp/pending-session");
    assertThat(toJson(body.getValue())).isEqualTo(json("{\"pendingRef\":\"somePendingRef\"}"));
  }

  @SuppressWarnings("unchecked")
  private <R> R call(Function<NOTPService, R> fn) {
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      return fn.apply(notpService);
    }
  }

  private Map<String, Object> body(String path) {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(NOTPResponse.class));
    assertThat(uri.getValue().toString()).isEqualTo(BASE_URL + path);
    return toJson(body.getValue());
  }

  private static Map<String, Object> toJson(Object body) {
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    return mapper.convertValue(body, new TypeReference<Map<String, Object>>() {});
  }

  private static Map<String, Object> json(String s) {
    try {
      return new ObjectMapper().readValue(s, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
