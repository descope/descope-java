package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.enums.NOTPProvider;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class NOTPServiceImplTest {
  private static final String BASE_URL = "https://api.descope.com";
  private static final String AUTH_MANAGEMENT_KEY = "someAuthManagementKey";
  private static final NOTPResponse MOCK_NOTP_RESPONSE =
      new NOTPResponse("somePendingRef", "https://wa.me/1234567890?text=abc", "someImage");

  /** Captures one POST: its path, JSON body and Authorization header. */
  private static class Captured {
    String header;
    URI uri;
    Map<String, Object> body;
    Object result;
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static Captured call(String authManagementKey, Object response, Function<NOTPService, Object> fn) {
    Client client = Client.builder()
        .uri(BASE_URL)
        .projectId(PROJECT_ID)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    NOTPService service = AuthenticationServiceBuilder.buildServices(client).getNotpService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    AtomicReference<String> header = new AtomicReference<>();
    Captured captured = new Captured();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      mockedJwtUtils.when(() -> JwtUtils.isJWTRequired(any())).thenCallRealMethod();
      captured.result = fn.apply(service);
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(response.getClass()));
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    captured.uri = uri.getValue();
    captured.body = mapper.readValue(mapper.writeValueAsString(body.getValue()), Map.class);
    captured.header = header.get();
    return captured;
  }

  private static Captured call(Function<NOTPService, Object> fn) {
    return call(null, MOCK_NOTP_RESPONSE, fn);
  }

  private static void assertNOTPResponse(Object result) {
    assertThat(result).isInstanceOf(NOTPResponse.class);
    NOTPResponse res = (NOTPResponse) result;
    assertThat(res.getPendingRef()).isEqualTo("somePendingRef");
    assertThat(res.getRedirectUrl()).isEqualTo("https://wa.me/1234567890?text=abc");
    assertThat(res.getImage()).isEqualTo("someImage");
  }

  private static Map<String, Object> map(Object... kv) {
    Map<String, Object> m = new HashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      m.put((String) kv[i], kv[i + 1]);
    }
    return m;
  }

  private static NOTPService service() {
    Client client = Client.builder().uri(BASE_URL).projectId(PROJECT_ID)
        .sdkInfo(SdkInfo.builder().name("java").build()).build();
    return AuthenticationServiceBuilder.buildServices(client).getNotpService();
  }

  @Test
  void testSignIn() {
    LoginOptions loginOptions = LoginOptions.builder()
        .customClaims(Collections.singletonMap("k1", "v1"))
        .templateOptions(Collections.singletonMap("t1", "v1"))
        .revokeOtherSessions(true)
        .revokeOtherSessionsTypes(new String[] {"web"})
        .locale("en")
        .templateId("someTemplate")
        .jwt("notSent")
        .build();
    Captured c = call(s -> s.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/whatsapp/signin");
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(c.body).isEqualTo(map("loginId", MOCK_PHONE, "loginOptions", map(
        "customClaims", map("k1", "v1"), "templateOptions", map("t1", "v1"), "revokeOtherSessions", true,
        "revokeOtherSessionsTypes", Collections.singletonList("web"), "locale", "en",
        "templateId", "someTemplate")));
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignInWithoutLoginIdOrOptions() {
    Captured c = call(s -> s.signIn(NOTPProvider.WHATSAPP, null, null));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/whatsapp/signin");
    assertThat(c.body).isEmpty();
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignInStepUpSendsRefreshToken() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    Captured c = call(AUTH_MANAGEMENT_KEY, MOCK_NOTP_RESPONSE,
        s -> s.signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, loginOptions, MOCK_REFRESH_TOKEN));
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY);
    assertThat(c.body).isEqualTo(map("loginId", MOCK_PHONE, "loginOptions", map("stepup", true)));
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignInStepUpWithoutRefreshTokenFails() {
    LoginOptions stepup = LoginOptions.builder().stepup(true).build();
    LoginOptions mfa = LoginOptions.builder().mfa(true).build();
    assertThatThrownBy(() -> service().signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, stepup))
        .isInstanceOf(ServerCommonException.class).hasMessage("The refreshToken argument is invalid");
    assertThatThrownBy(() -> service().signIn(NOTPProvider.WHATSAPP, MOCK_PHONE, mfa, ""))
        .isInstanceOf(ServerCommonException.class).hasMessage("The refreshToken argument is invalid");
  }

  @Test
  void testSignInWithoutProviderFails() {
    assertThatThrownBy(() -> service().signIn(null, MOCK_PHONE, null))
        .isInstanceOf(ServerCommonException.class).hasMessage("The Provider argument is invalid");
  }

  @Test
  void testSignUp() {
    User user = User.builder().name("someName").email("a@b.com").givenName("given").build();
    SignUpOptions signUpOptions = SignUpOptions.builder()
        .customClaims(Collections.singletonMap("aa", "bb"))
        .templateOptions(Collections.singletonMap("cc", "dd"))
        .build();
    Captured c = call(AUTH_MANAGEMENT_KEY, MOCK_NOTP_RESPONSE,
        s -> s.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, user, signUpOptions));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/whatsapp/signup");
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY);
    assertThat(c.body).isEqualTo(map(
        "loginId", MOCK_PHONE,
        "phone", MOCK_PHONE,
        "user", map("name", "someName", "email", "a@b.com", "givenName", "given", "phone", MOCK_PHONE),
        "loginOptions", map("customClaims", map("aa", "bb"), "templateOptions", map("cc", "dd"))));
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignUpKeepsUserPhone() {
    User user = User.builder().phone("+972555555555").build();
    Captured c = call(s -> s.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, user));
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(c.body).isEqualTo(map(
        "loginId", MOCK_PHONE, "phone", "+972555555555", "user", map("phone", "+972555555555")));
  }

  @Test
  void testSignUpWithoutUser() {
    Captured c = call(s -> s.signUp(NOTPProvider.WHATSAPP, MOCK_PHONE, null));
    assertThat(c.body).isEqualTo(map("loginId", MOCK_PHONE, "phone", MOCK_PHONE, "user", map("phone", MOCK_PHONE)));
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignUpWithoutProviderFails() {
    assertThatThrownBy(() -> service().signUp(null, MOCK_PHONE, null))
        .isInstanceOf(ServerCommonException.class).hasMessage("The Provider argument is invalid");
  }

  @Test
  void testSignUpOrIn() {
    SignUpOptions signUpOptions = SignUpOptions.builder()
        .customClaims(Collections.singletonMap("aa", "bb"))
        .templateOptions(Collections.singletonMap("cc", "dd"))
        .build();
    Captured c = call(s -> s.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE, signUpOptions));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/whatsapp/signup-in");
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(c.body).isEqualTo(map("loginId", MOCK_PHONE, "loginOptions", map(
        "customClaims", map("aa", "bb"), "templateOptions", map("cc", "dd"))));
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignUpOrInWithoutOptions() {
    Captured c = call(s -> s.signUpOrIn(NOTPProvider.WHATSAPP, MOCK_PHONE));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/whatsapp/signup-in");
    assertThat(c.body).isEqualTo(map("loginId", MOCK_PHONE));
    assertNOTPResponse(c.result);
  }

  @Test
  void testSignUpOrInWithoutProviderFails() {
    assertThatThrownBy(() -> service().signUpOrIn(null, MOCK_PHONE))
        .isInstanceOf(ServerCommonException.class).hasMessage("The Provider argument is invalid");
  }

  @Test
  void testGetSession() {
    Captured c = call(AUTH_MANAGEMENT_KEY, MOCK_JWT_RESPONSE, s -> s.getSession("somePendingRef"));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/pending-session");
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY);
    assertThat(c.body).isEqualTo(map("pendingRef", "somePendingRef"));
    AuthenticationInfo info = (AuthenticationInfo) c.result;
    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getUser()).isEqualTo(MOCK_JWT_RESPONSE.getUser());
    assertThat(info.getFirstSeen()).isTrue();
  }

  @Test
  void testUpdateUserPhone() {
    UpdateOptions updateOptions = new UpdateOptions(true, true);
    Captured c = call(AUTH_MANAGEMENT_KEY, MOCK_NOTP_RESPONSE, s -> s.updateUserPhone(NOTPProvider.WHATSAPP,
        "someLoginId", MOCK_PHONE, MOCK_REFRESH_TOKEN, updateOptions, Collections.singletonMap("cc", "dd")));
    assertThat(c.uri.toString()).isEqualTo(BASE_URL + "/v1/auth/notp/whatsapp/update");
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY);
    assertThat(c.body).isEqualTo(map("loginId", "someLoginId", "phone", MOCK_PHONE, "addToLoginIDs", true,
        "onMergeUseExisting", true, "templateOptions", map("cc", "dd")));
    assertNOTPResponse(c.result);
  }

  @Test
  void testUpdateUserPhoneWithoutOptions() {
    Captured c = call(s -> s.updateUserPhone(NOTPProvider.WHATSAPP, "someLoginId", MOCK_PHONE,
        MOCK_REFRESH_TOKEN, null));
    assertThat(c.header).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(c.body).isEqualTo(map("loginId", "someLoginId", "phone", MOCK_PHONE));
    assertNOTPResponse(c.result);
  }

  @Test
  void testUpdateUserPhoneInvalidArguments() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThatThrownBy(() -> service().updateUserPhone(NOTPProvider.WHATSAPP, "", MOCK_PHONE,
          MOCK_REFRESH_TOKEN, null)).isInstanceOf(ServerCommonException.class)
          .hasMessage("The Login ID argument is invalid");
      assertThatThrownBy(() -> service().updateUserPhone(NOTPProvider.WHATSAPP, "someLoginId", MOCK_PHONE,
          "", null)).isInstanceOf(ServerCommonException.class)
          .hasMessage("The Refresh Token argument is invalid");
      assertThatThrownBy(() -> service().updateUserPhone(null, "someLoginId", MOCK_PHONE,
          MOCK_REFRESH_TOKEN, null)).isInstanceOf(ServerCommonException.class)
          .hasMessage("The Provider argument is invalid");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }
}
