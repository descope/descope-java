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
          + "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCME/Mz+6rLT3gZ\n"
          + "U3TRyvk/fGoN7qe0RFH9dvLBkwocM3HS1R9clWiXu6bx3vgSgnusHVPpuvJ94R92\n"
          + "WWJAZJET0Cjlvn2lTPuSXfAZ5XkJqO7skoLhVRCvOQ/AH32ULyAfiv3NlPhwXwF9\n"
          + "7xq4pRMDOQUhfOcoZQt25Fpsz28lkL+Aqub5p0JgsdEFMkVdxX7IpyZm6vmSKrIj\n"
          + "asg1Mb9+0Rzvvci5JnJN1x4Tg8eeSajALWESI4wABSi9/qhhF8alPUQWuCguzPdn\n"
          + "Xp8idccqczgfnp5dhoCPqVUwcZtmpFsmuZfQnmXIVIoI1HN229ndeH+i+zDtVl/G\n"
          + "JWRVV8m/AgMBAAECggEABtWUWJVvTFMYw3eQbAnwpjnOPNbHhOKOc4ThrDMd26lg\n"
          + "5M30JUK+4yRLLWLZDKIkIY4ewXkXCrA4pkAPLKAbefF+hVMJc06xuX7uz3ykLqX4\n"
          + "3j75tr+9PCdXDuMab+fqs89GuciKZIUmH/xR++9F4bLe/rbG2nAvooHarZNjU1xN\n"
          + "mPEy/lyqxJlYITsGZeP4XDD0qGCU7onpOa1ej4EqWnLWQvQVxHmwOFvEETiXHyTA\n"
          + "kGYjkjnhrC320KL9u/OvLdRjuiFhMP1W4XpvKTg/EEXhGooMI7mx74/mLiUcFbeq\n"
          + "vzsbeH1MMRZvrRT4xHXWO4MPkM3426pTxnI+mWKoyQKBgQC/DHYv5yyADdwDz7+I\n"
          + "rdOvYBwJMgEZqCf6knq6D/XTk8I1X0utiQOitWYMW3PqS1xcXcrLfi+Ru6dlEbwk\n"
          + "aFOIIiU6tURlB+8XdowpHmccUIgVL7E5X5TstQCD8pcFkds3C0OV8Uuojm3U9EWM\n"
          + "GDYwc8UIrHouxl2RYQyn5ns5xwKBgQC7s1n9SDqJ27FvVFcGboEFo0i23C9iXcss\n"
          + "K5IpsnJBYDbrQ47UumhUYB5pG7orkPxLIZkmOMP8+/qRObSx7qUrYELdlbl4lYK7\n"
          + "4xsvyeY2jxCJGoItKqG+BsmA5TOOcHJ55wga9tcRa89ZFkSJqXUMYd8XswIYo4jN\n"
          + "sUZJu6kwSQKBgCEQOQwFSBBJVcBbHdy7qJz7Vx7IRqR4E6Mr4o184aBiPAQcn+5C\n"
          + "fhyUpDqTQTZVIIDjwosBJZ5lCY1WSmdKnto6fpLweAu8GcbHv24GUvX1gfeYr2Us\n"
          + "g4uLr7EmNLjEC1o1WtcCUYO0UpG+TKL4Nbig8IKVxvd9YQgd8aDFJKJVAoGAbVCO\n"
          + "4Evoi9E/DS95X22MBtSJzLV/gzJM2XSms1IR6LE4StwVmx7VFA7Gp0BWtHjD9p2i\n"
          + "q/fzbKrzyxBohBgQaaquRo6kbe3lLbeeZb6YLL41SyP9HIDvodQiFsdlt1cV1JkQ\n"
          + "x2Nq7eJz59ZoJCRk3slBPHQsjFzxl5ne7aI1bsECgYBmYUaBVLyycj1NaTeKAVED\n"
          + "gYyGRvuYEvffrhFLIzgvrm32hUAbaQ7GJ/LlJZ1jzLSrj1zKPp7WwZiGZwUrWO+v\n"
          + "zOJQZ3JvnxGrhCOPOcirAyVXuQB3cUhSLSqCZlppaH4grJiAy4Hsmym3bPn2C1OU\n"
          + "0tlYYB3U3TigQoZxseUNOw==\n"
          + "-----END PRIVATE KEY-----\n";
  
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
        .authManagementKey(EnvironmentUtils.getAuthManagementKey())
        .sdkInfo(getSdkInfo())
        .build();
  }

  public static String getRandomName(String prefix) {
    return prefix + UUID.randomUUID().toString();
  }

}