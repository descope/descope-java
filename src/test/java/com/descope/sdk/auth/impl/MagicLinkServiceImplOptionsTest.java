package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_MASKED_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_MASKED_PHONE;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
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

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.MagicLinkSignInOptions;
import com.descope.model.magiclink.MagicLinkSignUpOptions;
import com.descope.model.magiclink.MagicLinkUpdateOptions;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class MagicLinkServiceImplOptionsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final MagicLinkSignInOptions SIGN_IN_OPTIONS = MagicLinkSignInOptions.builder()
      .providerId("some-provider").templateId("some-template").ssoAppId("some-sso-app").useRecovery(true).build();
  private static final MagicLinkUpdateOptions UPDATE_OPTIONS = MagicLinkUpdateOptions.builder()
      .providerId("some-provider").templateId("some-template").locale("en").failOnConflict(true).build();

  @Test
  void testSignInWithOptions() {
    Sent sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_URL, null, null, SIGN_IN_OPTIONS),
        MOCK_MASKED_EMAIL);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signin/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body).isEqualTo(json("{\"loginId\":\"" + MOCK_EMAIL + "\",\"redirectUrl\":\"" + MOCK_URL
        + "\",\"providerId\":\"some-provider\",\"templateId\":\"some-template\",\"ssoAppId\":\"some-sso-app\","
        + "\"useRecovery\":true}"));
  }

  @Test
  void testSignInWithOptionsStepUpSendsRefreshJwt() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    Sent sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        s -> s.signIn(DeliveryMethod.SMS, MOCK_PHONE, null, MOCK_REFRESH_TOKEN, loginOptions,
            MagicLinkSignInOptions.builder().providerId("some-provider").build()),
        MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signin/sms");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.body.get("providerId").asText()).isEqualTo("some-provider");
    assertThat(sent.body.get("loginOptions").get("stepup").asBoolean()).isTrue();
    assertThat(sent.body.has("redirectUrl")).isFalse();
  }

  @Test
  void testSignInWithoutOptionsKeepsBody() {
    Sent sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_URL, null, null), MOCK_MASKED_EMAIL);
    assertThat(sent.body).isEqualTo(json("{\"URI\":\"" + MOCK_URL + "\",\"loginId\":\"" + MOCK_EMAIL + "\"}"));
  }

  @Test
  void testSignInWithOptionsForEmptyLoginId() {
    assertRejected(s -> s.signIn(DeliveryMethod.EMAIL, "", MOCK_URL, null, null, SIGN_IN_OPTIONS), "Login ID");
  }

  @Test
  void testSignInWithOptionsStepUpWithoutToken() {
    assertRejected(s -> s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_URL, null,
        LoginOptions.builder().mfa(true).build(), SIGN_IN_OPTIONS), "token");
  }

  @Test
  void testSignUpOrInWithOptions() {
    Sent sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        s -> s.signUpOrIn(DeliveryMethod.WHATSAPP, MOCK_PHONE, MOCK_URL, SIGN_IN_OPTIONS), MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signup-in/whatsapp");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body).isEqualTo(json("{\"loginId\":\"" + MOCK_PHONE + "\",\"redirectUrl\":\"" + MOCK_URL
        + "\",\"providerId\":\"some-provider\",\"templateId\":\"some-template\",\"ssoAppId\":\"some-sso-app\","
        + "\"useRecovery\":true}"));
  }

  @Test
  void testSignUpOrInWithOptionsForEmptyLoginId() {
    assertRejected(s -> s.signUpOrIn(DeliveryMethod.EMAIL, " ", MOCK_URL, SIGN_IN_OPTIONS), "Login ID");
  }

  @Test
  void testSignUpWithOptions() {
    User user = User.builder().email(MOCK_EMAIL).build();
    Sent sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_URL, user, null,
            MagicLinkSignUpOptions.builder().providerId("some-provider").templateId("some-template").build()),
        MOCK_MASKED_EMAIL);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signup/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body.get("email").asText()).isEqualTo(MOCK_EMAIL);
    assertThat(sent.body.get("loginId").asText()).isEqualTo(MOCK_EMAIL);
    assertThat(sent.body.get("user").get("email").asText()).isEqualTo(MOCK_EMAIL);
    assertThat(sent.body.get("redirectUrl").asText()).isEqualTo(MOCK_URL);
    assertThat(sent.body.get("providerId").asText()).isEqualTo("some-provider");
    assertThat(sent.body.get("templateId").asText()).isEqualTo("some-template");
    assertThat(sent.body.has("URI")).isFalse();
  }

  @Test
  void testSignUpWithOptionsForEmptyLoginId() {
    assertRejected(s -> s.signUp(DeliveryMethod.EMAIL, "", MOCK_URL, new User(), null,
        new MagicLinkSignUpOptions()), "Login ID");
  }

  @Test
  void testSignUpWithOptionsForInvalidPhone() {
    assertRejected(s -> s.signUp(DeliveryMethod.SMS, "not-a-phone", MOCK_URL, new User(), null,
        new MagicLinkSignUpOptions()), "user.phone");
  }

  @Test
  void testUpdateUserEmailWithOptions() {
    Sent sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.updateUserEmail("some-login", MOCK_EMAIL, MOCK_URL, MOCK_REFRESH_TOKEN,
            UpdateOptions.builder().addToLoginIds(true).build(), Collections.singletonMap("k", "v"), UPDATE_OPTIONS),
        MOCK_MASKED_EMAIL);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/update/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.body).isEqualTo(json("{\"email\":\"" + MOCK_EMAIL + "\",\"loginId\":\"some-login\","
        + "\"redirectUrl\":\"" + MOCK_URL + "\",\"addToLoginIDs\":true,\"onMergeUseExisting\":false,"
        + "\"templateOptions\":{\"k\":\"v\"},\"providerId\":\"some-provider\",\"templateId\":\"some-template\","
        + "\"locale\":\"en\",\"failOnConflict\":true}"));
  }

  @Test
  void testUpdateUserEmailWithoutOptionsKeepsBody() {
    Sent sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.updateUserEmail("some-login", MOCK_EMAIL, MOCK_URL, MOCK_REFRESH_TOKEN, null), MOCK_MASKED_EMAIL);
    assertThat(sent.body).isEqualTo(json("{\"email\":\"" + MOCK_EMAIL + "\",\"loginId\":\"some-login\","
        + "\"URI\":\"" + MOCK_URL + "\",\"crossDevice\":false,\"addToLoginIDs\":false,"
        + "\"onMergeUseExisting\":false}"));
  }

  @Test
  void testUpdateUserEmailWithOptionsForEmptyLoginId() {
    assertRejected(s -> s.updateUserEmail("", MOCK_EMAIL, MOCK_URL, MOCK_REFRESH_TOKEN, null, null,
        UPDATE_OPTIONS), "Login ID");
  }

  @Test
  void testUpdateUserEmailWithOptionsForInvalidEmail() {
    assertRejected(s -> s.updateUserEmail("some-login", "not-an-email", MOCK_URL, MOCK_REFRESH_TOKEN, null, null,
        UPDATE_OPTIONS), "Email");
  }

  @Test
  void testUpdateUserEmailWithOptionsForEmptyRefreshToken() {
    assertRejected(s -> s.updateUserEmail("some-login", MOCK_EMAIL, MOCK_URL, "", null, null, UPDATE_OPTIONS),
        "Refresh Token");
  }

  @Test
  void testUpdateUserPhoneWithOptions() {
    Sent sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        s -> s.updateUserPhone(DeliveryMethod.WHATSAPP, "some-login", MOCK_PHONE, MOCK_URL, MOCK_REFRESH_TOKEN,
            UpdateOptions.builder().onMergeUseExisting(true).build(), null, UPDATE_OPTIONS),
        MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/update/phone/whatsapp");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.body).isEqualTo(json("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"some-login\","
        + "\"redirectUrl\":\"" + MOCK_URL + "\",\"addToLoginIDs\":false,\"onMergeUseExisting\":true,"
        + "\"providerId\":\"some-provider\",\"templateId\":\"some-template\",\"locale\":\"en\","
        + "\"failOnConflict\":true}"));
  }

  @Test
  void testUpdateUserPhoneWithOptionsForEmptyLoginId() {
    assertRejected(s -> s.updateUserPhone(DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_URL, MOCK_REFRESH_TOKEN, null,
        null, UPDATE_OPTIONS), "Login ID");
  }

  @Test
  void testUpdateUserPhoneWithOptionsForInvalidPhone() {
    assertRejected(s -> s.updateUserPhone(DeliveryMethod.SMS, "some-login", "abc", MOCK_URL, MOCK_REFRESH_TOKEN,
        null, null, UPDATE_OPTIONS), "Phone");
  }

  @Test
  void testUpdateUserPhoneWithOptionsForInvalidMethod() {
    assertRejected(s -> s.updateUserPhone(DeliveryMethod.EMAIL, "some-login", MOCK_PHONE, MOCK_URL,
        MOCK_REFRESH_TOKEN, null, null, UPDATE_OPTIONS), "Method");
  }

  @Test
  void testUpdateUserPhoneWithOptionsForEmptyRefreshToken() {
    assertRejected(s -> s.updateUserPhone(DeliveryMethod.SMS, "some-login", MOCK_PHONE, MOCK_URL, "", null, null,
        UPDATE_OPTIONS), "Refresh Token");
  }

  @SneakyThrows
  @Test
  void testVerifyReturnsAllResponseFields() {
    JWTResponse jwtResponse = new JWTResponse(MOCK_JWT_RESPONSE.getSessionJwt(), MOCK_JWT_RESPONSE.getRefreshJwt(),
        "", "/", 1, 2, MOCK_JWT_RESPONSE.getUser(), true, null, null);
    jwtResponse.setSessionExpiration(3);
    jwtResponse.setExternalToken("some-external-token");
    jwtResponse.setClaims(Collections.singletonMap("c", "v"));
    jwtResponse.setTenantSSOID("some-tenant");
    jwtResponse.setTrustedDeviceJwt("some-trusted-device-jwt");
    jwtResponse.setNextRefreshSeconds(4);
    jwtResponse.setCookieName("DSR");
    jwtResponse.setSessionCookieName("DS");
    jwtResponse.setSessionCookieDomain("example.com");
    jwtResponse.setUnsavedSSO(true);
    jwtResponse.setKnownDeviceJwt("some-known-device-jwt");
    jwtResponse.setUserCreationDeferred(true);
    jwtResponse.setExternalPasskey(true);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    AuthenticationInfo info;
    String[] auth = new String[1];
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth[0] = getAuth(inv.getArgument(0));
        return apiProxy;
      });
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = service().verify("some-token");
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/magiclink/verify");
    assertThat(auth[0]).isEqualTo("Bearer " + PROJECT_ID);
    assertThat((JsonNode) MAPPER.valueToTree(body.getValue())).isEqualTo(json("{\"token\":\"some-token\"}"));

    assertThat(info.getToken()).isNotNull();
    assertThat(info.getRefreshToken()).isNotNull();
    assertThat(info.getUser()).isNotNull();
    assertThat(info.getFirstSeen()).isTrue();
    assertThat(info.getSessionExpiration()).isEqualTo(3);
    assertThat(info.getExternalToken()).isEqualTo("some-external-token");
    assertThat(info.getClaims()).containsEntry("c", "v");
    assertThat(info.getTenantSSOID()).isEqualTo("some-tenant");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("some-trusted-device-jwt");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(4);
    assertThat(info.getCookieName()).isEqualTo("DSR");
    assertThat(info.getSessionCookieName()).isEqualTo("DS");
    assertThat(info.getSessionCookieDomain()).isEqualTo("example.com");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("some-known-device-jwt");
    assertThat(info.getUserCreationDeferred()).isTrue();
    assertThat(info.getExternalPasskey()).isTrue();
  }

  @SneakyThrows
  @Test
  void testJwtResponseParsesNewFields() {
    JWTResponse parsed = MAPPER.readValue("{\"sessionExpiration\":3,\"claims\":{\"c\":\"v\"},"
        + "\"tenantSSOID\":\"t\",\"unsavedSSO\":true,\"nextRefreshSeconds\":4}", JWTResponse.class);
    assertThat(parsed.getSessionExpiration()).isEqualTo(3);
    assertThat(parsed.getClaims()).containsEntry("c", "v");
    assertThat(parsed.getTenantSSOID()).isEqualTo("t");
    assertThat(parsed.getUnsavedSSO()).isTrue();
    assertThat(parsed.getNextRefreshSeconds()).isEqualTo(4);
  }

  private static final class Sent {
    private String auth;
    private String path;
    private JsonNode body;
  }

  private static MagicLinkService service() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return AuthenticationServiceBuilder.buildServices(client).getMagicLinkService();
  }

  @SuppressWarnings("unchecked")
  private static String getAuth(Object supplier) {
    return ((Supplier<String>) supplier).get();
  }

  private static Sent send(Object response, Function<MagicLinkService, String> call, String expected) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    Sent sent = new Sent();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.auth = getAuth(inv.getArgument(0));
        return apiProxy;
      });
      assertThat(call.apply(service())).isEqualTo(expected);
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), any());
    sent.path = uri.getValue().getPath();
    sent.body = MAPPER.valueToTree(body.getValue());
    return sent;
  }

  private static void assertRejected(Function<MagicLinkService, String> call, String argument) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThatThrownBy(() -> call.apply(service()))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining(argument);
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @SneakyThrows
  private static JsonNode json(String value) {
    return MAPPER.readTree(value);
  }
}
