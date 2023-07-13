package com.descope.sdk.auth.impl;

import com.descope.model.auth.AuthParams;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.utils.EnvironmentUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestAuthUtils {
  public static final String PROJECT_ID = EnvironmentUtils.getProjectId();
  public static final String MOCK_EMAIL = "username@domain.com";
  public static final String MOCK_MASKED_EMAIL = "u*******@domain.com";
  public static final String MOCK_DOMAIN = "https://www.domain.com";
  public static final String UPDATE_MOCK_EMAIL = "updateusername@domain.com";
  public static final String MOCK_PHONE = "+1-5555555555";
  public static final String MOCK_MASKED_PHONE = "+1-555XXXXX55";
  public static final String MOCK_REFRESH_TOKEN = "2423r4gftrhtyu7i78ujuiy978";
  public static final String MOCK_URL = "https://www.domain.com";
  public static final String MOCK_PWD = "somePassword1!";
  public static final String MOCK_NAME = "Some Name";
  public static final User MOCK_USER =
      User.builder().email(MOCK_EMAIL).name(MOCK_NAME).phone(MOCK_PHONE).build();
  public static final UserResponse MOCK_USER_RESPONSE =
      new UserResponse(
          "someUserId",
          List.of(MOCK_EMAIL),
          "someEmail@descope.com",
          true,
          "+1-555-555-5555",
          false,
          "someName",
          Collections.emptyList(),
          Collections.emptyList(),
          "enabled",
          "",
          false,
          0L,
          Collections.emptyMap(),
          false,
          false,
          Collections.emptyMap());
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
          .projectId(PROJECT_ID)
          .jwt("someJwtToken")
          .claims(Map.of("someClaim", 1))
          .build();
  @SuppressWarnings("checkstyle:LineLength")
  public static final SigningKey MOCK_SIGNING_KEY =
      SigningKey.builder()
          .e("AQAB")
          .kid(PROJECT_ID)
          .kty("RSA")
          .n(
              "w8b3KRCep717H4MdVbwYHeb0vr891Ok1BL_TmC0XFUIKjRoKsWOcUZ9BFd6wR_5mnJuE7M8ZjVQRCbRlVgnh6AsEL3JA9Z6c1TpURTIXZxSE6NbeB7IMLMn5HWW7cjbnG4WO7E1PUCT6zCcBVz6EhA925GIJpyUxuY7oqJG-6NoOltI0Ocm6M2_7OIFMzFdw42RslqyX6l-SDdo_ZLq-XtcsCVRyj2YvmXUNF4Vq1x5syPOEQ-SezkvpBcb5Szi0ULpW5CvX2ieHAeHeQ2x8gkv6Dn2AW_dllQ--ZO-QH2QkxEXlMVqilwAdbA0k6BBtSkMC-7kD3A86bGGplpzz5Q")
          .build();

  static AuthParams getAuthParams() {
    return AuthParams.builder().projectId(EnvironmentUtils.getProjectId()).build();
  }
}