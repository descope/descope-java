package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_DOMAIN;
import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_MASKED_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_MASKED_PHONE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.sdk.TestUtils.UPDATE_MOCK_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.MagicLinkSignInOptions;
import com.descope.model.magiclink.MagicLinkSignUpOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.MagicLinkTestUserResponse;
import com.descope.model.user.response.UserFamily;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.descope.utils.UriUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class MagicLinkServiceImplTest {

  private MagicLinkService magicLinkService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.magicLinkService =
        AuthenticationServiceBuilder.buildServices(client).getMagicLinkService();
    this.userService = ManagementServiceBuilder.buildServices(client).getUserService();
  }

  @Test
  void signUp() {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signUp = magicLinkService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, user);
      assertThat(signUp).isNotBlank().contains("*");
    }
  }

  @SneakyThrows
  @Test
  void testVerify() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = magicLinkService.verify("SomeToken");
      }
    }

    assertThat(authenticationInfo).isNotNull();

    Token sessionToken = authenticationInfo.getToken();
    assertThat(sessionToken).isNotNull();
    assertThat(sessionToken.getJwt()).isNotBlank();
    assertThat(sessionToken.getClaims()).isNotEmpty();
    assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);

    Token refreshToken = authenticationInfo.getRefreshToken();
    assertThat(refreshToken).isNotNull();
    assertThat(refreshToken.getJwt()).isNotBlank();
    assertThat(refreshToken.getClaims()).isNotEmpty();
    assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);

    UserResponse user = authenticationInfo.getUser();
    assertThat(user).isNotNull();
    assertThat(user.getUserId()).isNotBlank();
    assertThat(user.getLoginIds()).isNotEmpty();
  }

  @Test
  void signIn() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signIn =
          magicLinkService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null, null);
      assertThat(signIn).isNotBlank().contains("*");
    }
  }

  @Test
  void testSignUpOrInForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signUpOrIn =
          magicLinkService.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN);
      assertThat(signUpOrIn).isNotBlank().contains("*");
    }
  }

  @Test
  void testSignUpOrInForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.signUpOrIn(DeliveryMethod.EMAIL, "", MOCK_DOMAIN));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyLoginId() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail("", MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail(MOCK_EMAIL, "", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForInvalidEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyRefreshToken() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN, "", null));

    assertNotNull(thrown);
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String updateUserEmail =
          magicLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null);
      assertThat(updateUserEmail).isNotBlank().contains("*");
    }
  }

  @Test
  void testUpdateUserPhoneForLoginID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForEmptyPhone() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.SMS, MOCK_EMAIL, "", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForInvalidPhone() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.SMS, MOCK_EMAIL, "1234E", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForInvalidMethod() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedPhoneRes maskedEmailRes = new MaskedPhoneRes(MOCK_MASKED_PHONE);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String updateUserPhone =
          magicLinkService.updateUserPhone(
            DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null);
      assertThat(updateUserPhone).isNotBlank().contains("X");
    }
  }

  @Test
  void testSignInWithAllOptions() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).templateId("tmpl").locale("en")
        .ssoAppId("loginApp").pkceChallenge("Y2hhbGxlbmdl").tenantId("T1").build();
    MagicLinkSignInOptions signInOptions = MagicLinkSignInOptions.builder().providerId("prov")
        .templateId("topTmpl").ssoAppId("app").useRecovery(true).build();
    SentRequest sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE), s -> s.signIn(
        DeliveryMethod.WHATSAPP, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, loginOptions, signInOptions));

    assertThat(sent.result).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signin/whatsapp");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.body).containsEntry("loginId", MOCK_PHONE).containsEntry("URI", MOCK_DOMAIN)
        .containsEntry("providerId", "prov").containsEntry("templateId", "topTmpl")
        .containsEntry("ssoAppId", "app").containsEntry("useRecovery", true);
    assertThat(asMap(sent.body.get("loginOptions"))).containsEntry("stepup", true)
        .containsEntry("templateId", "tmpl").containsEntry("locale", "en").containsEntry("ssoAppId", "loginApp")
        .containsEntry("pkceChallenge", "Y2hhbGxlbmdl").containsEntry("tenantId", "T1");
  }

  @Test
  void testSignInWithoutOptionsSendsSameBody() {
    SentRequest sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null, null));

    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signin/email");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body).containsOnlyKeys("loginId", "URI");
  }

  @Test
  void testSignUpOrInWithAllOptions() {
    LoginOptions loginOptions = LoginOptions.builder().templateId("tmpl").tenantId("T1").ssoAppId("loginApp")
        .pkceChallenge("Y2hhbGxlbmdl").build();
    MagicLinkSignInOptions signInOptions = MagicLinkSignInOptions.builder().providerId("prov")
        .templateId("topTmpl").ssoAppId("app").useRecovery(false).build();
    SentRequest sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE), s -> s.signUpOrIn(
        DeliveryMethod.SMS, MOCK_PHONE, MOCK_DOMAIN, loginOptions, signInOptions));

    assertThat(sent.result).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signup-in/sms");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body).containsEntry("loginId", MOCK_PHONE).containsEntry("providerId", "prov")
        .containsEntry("templateId", "topTmpl").containsEntry("ssoAppId", "app")
        .containsEntry("useRecovery", false);
    assertThat(asMap(sent.body.get("loginOptions"))).containsEntry("templateId", "tmpl")
        .containsEntry("tenantId", "T1").containsEntry("ssoAppId", "loginApp")
        .containsEntry("pkceChallenge", "Y2hhbGxlbmdl");
  }

  @Test
  void testSignUpOrInWithoutOptionsSendsSameBody() {
    SentRequest sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN));

    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signup-in/email");
    assertThat(sent.body).containsOnlyKeys("loginId", "URI");
  }

  @Test
  void testSignUpWithAllOptions() {
    User user = User.builder().name(MOCK_NAME).phone(MOCK_PHONE).loginId(MOCK_PHONE)
        .recoveryEmail(MOCK_EMAIL).recoveryPhone("+15555555556").build();
    SignUpOptions signUpOptions = SignUpOptions.builder().locale("en").pkceChallenge("Y2hhbGxlbmdl")
        .templateId("tmpl").tenantId("T1").build();
    MagicLinkSignUpOptions requestOptions = MagicLinkSignUpOptions.builder().phone(MOCK_PHONE)
        .providerId("prov").templateId("topTmpl").build();
    SentRequest sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE), s -> s.signUp(
        DeliveryMethod.SMS, MOCK_PHONE, MOCK_DOMAIN, user, signUpOptions, requestOptions));

    assertThat(sent.result).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signup/sms");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body).containsEntry("loginId", MOCK_PHONE).containsEntry("phone", MOCK_PHONE)
        .containsEntry("providerId", "prov").containsEntry("templateId", "topTmpl");
    assertThat(asMap(sent.body.get("user"))).containsEntry("loginId", MOCK_PHONE)
        .containsEntry("recoveryEmail", MOCK_EMAIL).containsEntry("recoveryPhone", "+15555555556");
    assertThat(asMap(sent.body.get("loginOptions"))).containsEntry("locale", "en")
        .containsEntry("pkceChallenge", "Y2hhbGxlbmdl").containsEntry("templateId", "tmpl")
        .containsEntry("tenantId", "T1");
  }

  @Test
  void testSignUpWithoutOptionsSendsSameBody() {
    User user = new User(MOCK_NAME, MOCK_EMAIL, MOCK_PHONE);
    SentRequest sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, user));

    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/signup/email");
    assertThat(sent.body).containsOnlyKeys("email", "loginId", "user", "URI");
    assertThat(asMap(sent.body.get("user"))).containsOnlyKeys("name", "email", "phone");
  }

  @Test
  void testSignUpEmailWithPhone() {
    MagicLinkSignUpOptions requestOptions = MagicLinkSignUpOptions.builder().phone(MOCK_PHONE).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> magicLinkService.signUp(
        DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null, null, requestOptions));
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithAllOptions() {
    UpdateOptions updateOptions = UpdateOptions.builder().addToLoginIds(true).providerId("prov")
        .templateId("tmpl").locale("en").failOnConflict(true).build();
    SentRequest sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL), s -> s.updateUserEmail(
        MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, updateOptions));

    assertThat(sent.result).isEqualTo(MOCK_MASKED_EMAIL);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/update/email");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.body).containsEntry("email", UPDATE_MOCK_EMAIL).containsEntry("addToLoginIDs", true)
        .containsEntry("providerId", "prov").containsEntry("templateId", "tmpl").containsEntry("locale", "en")
        .containsEntry("failOnConflict", true);
  }

  @Test
  void testUpdateUserEmailWithoutOptionsSendsSameBody() {
    SentRequest sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL), s -> s.updateUserEmail(
        MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertThat(sent.body).containsOnlyKeys(
        "email", "loginId", "URI", "crossDevice", "addToLoginIDs", "onMergeUseExisting");
  }

  @Test
  void testUpdateUserPhoneWithAllOptions() {
    UpdateOptions updateOptions = UpdateOptions.builder().providerId("prov").templateId("tmpl").locale("en")
        .failOnConflict(false).build();
    SentRequest sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE), s -> s.updateUserPhone(
        DeliveryMethod.WHATSAPP, MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, updateOptions));

    assertThat(sent.result).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(sent.path).isEqualTo("/v1/auth/magiclink/update/phone/whatsapp");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.body).containsEntry("phone", MOCK_PHONE).containsEntry("providerId", "prov")
        .containsEntry("templateId", "tmpl").containsEntry("locale", "en").containsEntry("failOnConflict", false);
  }

  @SneakyThrows
  @Test
  void testVerifyFullResponse() {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"d\",\"cookiePath\":\"/\","
        + "\"cookieMaxAge\":10,\"cookieExpiration\":11,\"firstSeen\":true,\"sessionExpiration\":12,"
        + "\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"sso\",\"trustedDeviceJwt\":\"td\","
        + "\"nextRefreshSeconds\":13,\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\","
        + "\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kd\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\"},"
        + "\"user\":{\"userId\":\"u1\",\"userTenants\":[{\"tenantId\":\"T1\",\"permissions\":[\"p\"],"
        + "\"roleIds\":[\"r\"]}],\"externalIds\":[\"e\"],\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true},"
        + "\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,"
        + "\"permissions\":[\"up\"],\"OIDC\":true,\"consentExpiration\":14,\"recoveryEmail\":\"re\","
        + "\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"rp\",\"verifiedRecoveryPhone\":true,"
        + "\"modifiedTime\":15,\"roleIds\":[\"ur\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\","
        + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":16,\"unknown\":1}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());

    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = magicLinkService.verify("SomeToken");
      }
    }

    assertThat(info.getCookieDomain()).isEqualTo("d");
    assertThat(info.getCookiePath()).isEqualTo("/");
    assertThat(info.getCookieMaxAge()).isEqualTo(10);
    assertThat(info.getCookieExpiration()).isEqualTo(11);
    assertThat(info.getSessionExpiration()).isEqualTo(12);
    assertThat(info.getExternalToken()).isEqualTo("ext");
    assertThat(info.getClaims()).containsEntry("c", "v");
    assertThat(info.getTenantSSOID()).isEqualTo("sso");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("td");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(13);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kd");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();
    assertThat(info.getFirstSeen()).isTrue();

    IDPResponse idp = info.getIdpResponse();
    assertThat(idp.getSamlResponse()).isEqualTo("sr");
    assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(idp.getOidcResponse()).isEqualTo("or");
    assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = info.getUser();
    assertThat(user.getUserTenants().get(0).getPermissions()).containsExactly("p");
    assertThat(user.getUserTenants().get(0).getRoleIds()).containsExactly("r");
    assertThat(user.getExternalIds()).containsExactly("e");
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getSCIM()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("up");
    assertThat(user.getOIDC()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(14L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("rp");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(15L);
    assertThat(user.getRoleIds()).containsExactly("ur");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    UserFamily family = user.getUserFamilies().get(0);
    assertThat(family.getFamilyId()).isEqualTo("f");
    assertThat(family.getRoleNames()).containsExactly("fr");
    assertThat(family.getPermissions()).containsExactly("fp");
    assertThat(family.getFamilyScopedAttributes()).containsEntry("a", "b");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(16L);
  }

  @SneakyThrows
  @Test
  void testVerifyNullObjects() {
    JWTResponse jwtResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue("{\"sessionJwt\":\"s\",\"user\":null,\"idpResponse\":null,\"claims\":null}", JWTResponse.class);
    assertThat(jwtResponse.getUser()).isNull();
    assertThat(jwtResponse.getIdpResponse()).isNull();
    assertThat(jwtResponse.getClaims()).isNull();
  }

  @SneakyThrows
  @Test
  void testAssociatedTenantResponseFieldsNotSent() {
    AssociatedTenant tenant = AssociatedTenant.builder().tenantId("T1").permissions(Arrays.asList("p"))
        .roleIds(Arrays.asList("r")).build();
    Map<String, Object> sent = asMap(tenant);
    assertThat(sent).containsOnlyKeys("tenantId");
  }

  private static final Client CLIENT = Client.builder()
      .uri("https://api.descope.com")
      .projectId(PROJECT_ID)
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build();

  private static class SentRequest {
    private String path;
    private String authorization;
    private Map<String, Object> body;
    private Object result;
  }

  /** Runs the call against a mocked proxy and returns what it sent. */
  @SuppressWarnings("unchecked")
  private static SentRequest send(Object response, Function<MagicLinkService, Object> call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    SentRequest sent = new SentRequest();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.authorization = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      sent.result = call.apply(AuthenticationServiceBuilder.buildServices(CLIENT).getMagicLinkService());
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), any(Class.class));
    sent.path = uriCaptor.getValue().getPath();
    sent.body = asMap(bodyCaptor.getValue());
    return sent;
  }

  // Serializes the way the proxy does
  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    return objectMapper.readValue(objectMapper.writeValueAsBytes(value), Map.class);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    MagicLinkTestUserResponse response =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.EMAIL);
    assertThat(response.getLink()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    AuthenticationInfo authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdateEmail() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    MagicLinkTestUserResponse response =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.EMAIL);
    assertThat(response.getLink()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    AuthenticationInfo authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    MagicLinkTestUserResponse response2 =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.EMAIL);
    assertThat(response2.getLink()).isNotBlank();
    magicLinkService.updateUserEmail(
        loginId, loginId + "1@descope.com", MOCK_URL, authInfo.getRefreshToken().getJwt(), null);
    params = UriUtils.splitQuery("https://kuku.com" + response2.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdatePhone() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().phone(MOCK_PHONE).verifiedPhone(true).build());
    MagicLinkTestUserResponse response =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.SMS);
    assertThat(response.getLink()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    AuthenticationInfo authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    MagicLinkTestUserResponse response2 =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.SMS);
    assertThat(response2.getLink()).isNotBlank();
    magicLinkService.updateUserPhone(
        DeliveryMethod.SMS, loginId, "+1-555-555-5556", MOCK_URL, authInfo.getRefreshToken().getJwt(), null);
    params = UriUtils.splitQuery("https://kuku.com" + response2.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

}
