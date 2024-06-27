package com.descope.sdk;

import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.utils.EnvironmentUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.junit.platform.commons.util.StringUtils;

@UtilityClass
public class TestUtils {

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
  public static final User MOCK_USER = User.builder().email(MOCK_EMAIL).name(MOCK_NAME).phone(MOCK_PHONE).build();
  public static final UserResponse MOCK_USER_RESPONSE = new UserResponse(
      "someUserId",
      Arrays.asList(MOCK_EMAIL),
      "someEmail@descope.com",
      true,
      "+1-555-555-5555",
      false,
      "someName",
      null,
      null,
      null,
      Collections.emptyList(),
      Collections.emptyList(),
      "enabled",
      "",
      false,
      0L,
      Collections.emptyMap(),
      false,
      false,
      Collections.emptyMap(),
      Collections.emptyList());
  public static final JWTResponse MOCK_JWT_RESPONSE = new JWTResponse(
      "someSessionJwt",
      "someRefreshJwt",
      "",
      "/",
      1234567,
      1234567890,
      MOCK_USER_RESPONSE,
      true);
  public static final Map<String, Object> TENANTS_AUTHZ = mapOf("permissions", Arrays.asList("tp1", "tp2"), "roles",
      Arrays.asList("tr1", "tr2"));
  public static final Token MOCK_TOKEN = Token.builder()
      .id("1")
      .projectId(PROJECT_ID)
      .jwt("someJwtToken")
      .claims(mapOf("someClaim", 1,
          "tenants", mapOf("someTenant", TENANTS_AUTHZ),
          "permissions", Arrays.asList("p1", "p2"), "roles", Arrays.asList("r1", "r2")))
      .build();
  @SuppressWarnings("checkstyle:LineLength")
  public static final SigningKey MOCK_SIGNING_KEY = SigningKey.builder()
      .e("AQAB")
      .kid(PROJECT_ID)
      .kty("RSA")
      .n(
          "w8b3KRCep717H4MdVbwYHeb0vr891Ok1BL_TmC0XFUIKjRoKsWOcUZ9BFd6wR_5mnJuE7M8ZjVQRCbRlVgnh6AsEL3JA9Z6c1TpURTIXZxSE6NbeB7IMLMn5HWW7cjbnG4WO7E1PUCT6zCcBVz6EhA925GIJpyUxuY7oqJG-6NoOltI0Ocm6M2_7OIFMzFdw42RslqyX6l-SDdo_ZLq-XtcsCVRyj2YvmXUNF4Vq1x5syPOEQ-SezkvpBcb5Szi0ULpW5CvX2ieHAeHeQ2x8gkv6Dn2AW_dllQ--ZO-QH2QkxEXlMVqilwAdbA0k6BBtSkMC-7kD3A86bGGplpzz5Q")
      .build();
  public static final String MOCK_PRIVATE_KEY_STRING = "-----BEGIN PRIVATE KEY-----\n"
    + "MIIB1QIBADANBgkqhkiG9w0BAQEFAASCAb8wggG7AgEAAl0DH3YqFv4mzt67RAAm\n"
    + "KqZSY32GtoUqkLXzSJOIew2ofiKx3ojdJvL69pXZLKNoKkKb8RQKyWdhAIkbTEFX\n"
    + "3k8mroXea5NMfB9NAH0AASQ6uoK5XYs7mMubQgu1dhcCAwEAAQJdAjrb+LAUaQe8\n"
    + "+cFTze0UeK48Ow5nxn4wvniriIA9v3vaMGJ0Hl6qkFO1qq76O+uvSehxPHnzBrfs\n"
    + "SXkQ8nScyeGpoTpn0DCnMnFRiY1hAMy6SqVdC4t7UP9u6oCBAi8B+POU6nCyUOnL\n"
    + "FlPVGFoBxSoxC7q7tJytq+xaPfGBN63AT3sdnXm06YAH1uE/1wIvAZVPf+1sDjIP\n"
    + "c4hFNPzIPh/x1M3qDN9eBr6tdPwymuPmpQ1lik/b9ZpMfXGns8ECLwDTVfcci+BF\n"
    + "tyP1i06jq4AUKg1u8E+BTxXs37YBOOOxDvpvCYMiln6eP6SITavvAi8A6n71d8rl\n"
    + "p6by4+uOjZXZA6hpw7zfN7hx1I4MugEZRjPiWI7f5/ZN8bjBdylcwQIvAQp1f9vQ\n"
    + "S+P5ktRlO7vEm10LtKotJ85Rp+le7PX56re+nntKVZFsliKW0yPmWJE=\n"
    + "-----END PRIVATE KEY-----";
  private static SdkInfo getSdkInfo() {
    String name = "java";
    String version = System.getProperty("java.version");

    // TODO - SHA
    return SdkInfo.builder()
        .name(name)
        .javaVersion(version)
        .version(new SdkInfo().getClass().getPackage().getImplementationVersion())
        .build();
  }

  public static Client getClient() {
    String baseUrl = EnvironmentUtils.getBaseURL();
    if (StringUtils.isBlank(baseUrl)) {
      baseUrl = "https://api.descope.com";
    }
    return Client.builder()
        .uri(baseUrl)
        .projectId(EnvironmentUtils.getProjectId())
        .managementKey(EnvironmentUtils.getManagementKey())
        .sdkInfo(getSdkInfo())
        .build();
  }

  public static String getRandomName(String prefix) {
    return prefix + UUID.randomUUID().toString();
  }

}