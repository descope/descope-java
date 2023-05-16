package com.descope.sdk.impl;

import com.descope.enums.AuthType;
import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.utils.JwtUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.descope.sdk.impl.MagicLinkServiceImplTest.MOCK_MASKED_PHONE;
import static com.descope.sdk.impl.MagicLinkServiceImplTest.MOCK_PHONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OTPServiceImplTest {

    public static final String MOCK_PROJECT_ID = "someProjectId";
    public static final String MOCK_EMAIL = "username@domain.com";
    public static final String MOCK_MASKED_EMAIL = "u*******@domain.com";
    public static final String MOCK_DOMAIN = "https://www.domain.com";

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


    private OTPService otpService;

    @BeforeEach
    void setUp() {
        var authParams = AuthParams.builder().projectId(MOCK_PROJECT_ID).build();
        var client = Client.builder().uri("https://api.descope.com/v1").build();
        this.otpService =
                (OTPService) AuthenticationServiceBuilder.buildService(AuthType.OTP, client, authParams);
    }

    @Test
    void testSignUp() {
        User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

        var apiProxy = mock(ApiProxy.class);
        var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
        doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            String signUp = otpService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, user);
            Assertions.assertThat(signUp).isNotBlank().contains("*");
        }

    }

    @Test
    void signIn() {
        var apiProxy = mock(ApiProxy.class);
        var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
        doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            String signIn =
                    otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, null);
            Assertions.assertThat(signIn).isNotBlank().contains("*");
        }
    }

    @Test
    void testSignUpOrIn() {
        var apiProxy = mock(ApiProxy.class);
        var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
        doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            String signUpOrIn =
                    otpService.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL);
            Assertions.assertThat(signUpOrIn).isNotBlank().contains("*");
        }
    }

    @Test
    void testUpdateUserEmailForEmptyLoginId() {

        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> otpService.updateUserEmail("", MOCK_EMAIL));

        assertNotNull(thrown);
        assertEquals("The Login ID argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserEmailForEmptyEmail() {

        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> otpService.updateUserEmail(MOCK_EMAIL, ""));

        assertNotNull(thrown);
        assertEquals("The Email argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserEmailForInvalidEmail() {

        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> otpService.updateUserEmail(MOCK_EMAIL, "abc"));

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
            String updateUserEmail =
                    otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL);
            Assertions.assertThat(updateUserEmail).isNotBlank().contains("*");
        }
    }

    @Test
    void testUpdateUserPhoneForLoginID() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class,
                        () -> otpService.updateUserPhone(DeliveryMethod.SMS, "", MOCK_PHONE));
        assertNotNull(thrown);
        assertEquals("The Login ID argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserPhoneForEmptyPhone() {
        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () ->
                                otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, ""));
        assertNotNull(thrown);
        assertEquals("The Phone argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserPhoneForInvalidPhone() {
        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () ->
                                otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, "1234E"));
        assertNotNull(thrown);
        assertEquals("The Phone argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserPhoneForInvalidMethod() {
        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () ->
                                otpService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE));
        assertNotNull(thrown);
        assertEquals("The Method argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateUserPhoneForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        var maskedPhoneRes = new MaskedPhoneRes(MOCK_MASKED_PHONE);
        doReturn(maskedPhoneRes).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            String updateUserPhone =
                    otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE);
            Assertions.assertThat(updateUserPhone).isNotBlank().contains("X");
        }
    }

    @Test
    void testVerifyCode() {
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
                authenticationInfo = otpService.verifyCode(DeliveryMethod.EMAIL, MOCK_EMAIL, "somecode");
            }
        }

        Assertions.assertThat(authenticationInfo).isNotNull();

        Token sessionToken = authenticationInfo.getToken();
        Assertions.assertThat(sessionToken).isNotNull();
        Assertions.assertThat(sessionToken.getJwt()).isNotBlank();
        Assertions.assertThat(sessionToken.getClaims()).isNotEmpty();
        Assertions.assertThat(sessionToken.getProjectId()).isEqualTo(MOCK_PROJECT_ID);

        Token refreshToken = authenticationInfo.getRefreshToken();
        Assertions.assertThat(refreshToken).isNotNull();
        Assertions.assertThat(refreshToken.getJwt()).isNotBlank();
        Assertions.assertThat(refreshToken.getClaims()).isNotEmpty();
        Assertions.assertThat(refreshToken.getProjectId()).isEqualTo(MOCK_PROJECT_ID);

        UserResponse user = authenticationInfo.getUser();
        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getUserId()).isNotBlank();
        Assertions.assertThat(user.getLoginIds()).isNotEmpty();
    }

}
