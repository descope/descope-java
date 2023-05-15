package com.descope.sdk.impl;

import com.descope.enums.AuthType;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UserResponse;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.EnchantedLinkService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.utils.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class EnchantedLinkServiceImplTest {
    public static final String MOCK_PROJECT_ID = "someProjectId";
    public static final String MOCK_EMAIL = "username@domain.com";
    public static final String MOCK_MASKED_EMAIL = "u*******@domain.com";
    public static final String MOCK_DOMAIN = "https://www.domain.com";
    public static final String UPDATE_MOCK_EMAIL = "updateusername@domain.com";

    public static final String MOCK_PHONE = "+11-1234567890";

    public static final String MOCK_MASKED_PHONE = "+11-123XXXXX90";
    public static final String MOCK_REFRESH_TOKEN = "2423r4gftrhtyu7i78ujuiy978";

    public static final UserResponse MOCK_USER_RESPONSE =
            new UserResponse(
                    "someUserId",
                    List.of(MOCK_EMAIL),
                    true,
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "enabled",
                    "",
                    false);
    public static final JWTResponse MOCK_JWT_RESPONSE =
            new JWTResponse(
                    "someSessionJwt",
                    "someRefreshJwt",
                    "",
                    "/",
                    1234567,
                    1234567890,
                    MOCK_USER_RESPONSE,
                    true);
    public static final Token MOCK_TOKEN =
            Token.builder()
                    .id("1")
                    .projectId(MOCK_PROJECT_ID)
                    .jwt("someJwtToken")
                    .claims(Map.of("someClaim", 1))
                    .build();
    public static final SigningKey MOCK_SIGNING_KEY =
            SigningKey.builder()
                    .e("AQAB")
                    .kid(MOCK_PROJECT_ID)
                    .kty("RSA")
                    .n(
                            "w8b3KRCep717H4MdVbwYHeb0vr891Ok1BL_TmC0XFUIKjRoKsWOcUZ9BFd6wR_5mnJuE7M8ZjVQRCbRlVgnh6AsEL3JA9Z6c1TpURTIXZxSE6NbeB7IMLMn5HWW7cjbnG4WO7E1PUCT6zCcBVz6EhA925GIJpyUxuY7oqJG-6NoOltI0Ocm6M2_7OIFMzFdw42RslqyX6l-SDdo_ZLq-XtcsCVRyj2YvmXUNF4Vq1x5syPOEQ-SezkvpBcb5Szi0ULpW5CvX2ieHAeHeQ2x8gkv6Dn2AW_dllQ--ZO-QH2QkxEXlMVqilwAdbA0k6BBtSkMC-7kD3A86bGGplpzz5Q")
                    .build();

    private EnchantedLinkService enchantedLinkService;

    @BeforeEach
    void setUp() {
        var authParams = AuthParams.builder().projectId(MOCK_PROJECT_ID).build();
        var client = Client.builder().uri("https://api.descope.com/v1").build();
        this.enchantedLinkService = (EnchantedLinkService) AuthenticationServiceBuilder.buildService(AuthType.ENCHANTED_LINK, client, authParams);
    }

    @Test
    void signUp() {
        User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

        var apiProxy = mock(ApiProxy.class);
        doReturn(Mockito.mock(EnchantedLinkResponse.class)).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            EnchantedLinkResponse signUp = enchantedLinkService.signUp(MOCK_EMAIL, MOCK_DOMAIN, user);
            Assertions.assertNotNull(signUp);
        }
    }

    @Test
    void signIn() {
        var apiProxy = mock(ApiProxy.class);
        var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
        doReturn(Mockito.mock(EnchantedLinkResponse.class)).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            String signIn = String.valueOf(enchantedLinkService.signIn(MOCK_EMAIL, MOCK_DOMAIN, null));

        }
    }

    @Test
    void testSignUpOrInForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
        doReturn(Mockito.mock(EnchantedLinkResponse.class)).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            EnchantedLinkResponse signUpOrIn = enchantedLinkService.signUpOrIn(MOCK_EMAIL, MOCK_DOMAIN);
        }
    }

    @Test
    void testSignUpOrInForEmptyLoginId() {
        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> enchantedLinkService.signUpOrIn("", MOCK_DOMAIN));

        assertNotNull(thrown);
        assertEquals("The Login ID argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserEmailForEmptyLoginId() {

        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> enchantedLinkService.updateUserEmail("", MOCK_DOMAIN, null));

        assertNotNull(thrown);
        assertEquals("The Login ID argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserEmailForEmptyEmail() {

        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "", null));

        assertNotNull(thrown);
        assertEquals("The Email argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserEmailForInvalidEmail() {

        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "abc", null));

        assertNotNull(thrown);
        assertEquals("The Email argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserEmailForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
        doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            String updateUserEmail = enchantedLinkService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN);
            org.assertj.core.api.Assertions.assertThat(updateUserEmail).isNotBlank().contains("*");
        }
    }

    @Test
    void testGetSession() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
        doReturn(new SigningKey[]{MOCK_SIGNING_KEY}).when(apiProxy).get(any(), eq(SigningKey[].class));

        var provider = mock(Provider.class);
        when(provider.getProvidedKey()).thenReturn(mock(Key.class));

        AuthenticationInfo authenticationInfo;
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);

            try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
                mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
                authenticationInfo = enchantedLinkService.getSession("testRef");
            }
        }

        org.assertj.core.api.Assertions.assertThat(authenticationInfo).isNotNull();

        Token sessionToken = authenticationInfo.getToken();
        org.assertj.core.api.Assertions.assertThat(sessionToken).isNotNull();
        org.assertj.core.api.Assertions.assertThat(sessionToken.getJwt()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(sessionToken.getClaims()).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(sessionToken.getProjectId()).isEqualTo(MOCK_PROJECT_ID);

        Token refreshToken = authenticationInfo.getRefreshToken();
        org.assertj.core.api.Assertions.assertThat(refreshToken).isNotNull();
        org.assertj.core.api.Assertions.assertThat(refreshToken.getJwt()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(refreshToken.getClaims()).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(refreshToken.getProjectId()).isEqualTo(MOCK_PROJECT_ID);

        UserResponse user = authenticationInfo.getUser();
        org.assertj.core.api.Assertions.assertThat(user).isNotNull();
        org.assertj.core.api.Assertions.assertThat(user.getUserId()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(user.getLoginIds()).isNotEmpty();
    }

    @Test
    void testVerify() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(Mockito.mock(EmptyResponse.class)).when(apiProxy).post(any(), any(), any());
        doReturn(new SigningKey[]{MOCK_SIGNING_KEY}).when(apiProxy).get(any(), eq(SigningKey[].class));

        var provider = mock(Provider.class);
        when(provider.getProvidedKey()).thenReturn(mock(Key.class));

        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);

            try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
                mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
                enchantedLinkService.verify("token");
            }
        }

    }
}
