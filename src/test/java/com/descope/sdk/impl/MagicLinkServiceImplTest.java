package com.descope.sdk.impl;

import com.descope.enums.DeliveryMethod;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.UserResponse;
import com.descope.model.client.Client;
import com.descope.model.jwt.JWTResponse;
import com.descope.model.jwt.Token;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MagicLinkServiceImplTest {

  public static final String PROJECT_ID = "P2NxG0ZaqijHrnC6agWzWuBp5oXL";

  public static final UserResponse MOCK_USER_RESPONSE =
      new UserResponse(
          "U2ObRjQqqjxgl2nYyZXi29Vxp6VW",
          List.of("kuduwa.keshavram@gmail.com"),
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
          2419199,
          1684246629,
          MOCK_USER_RESPONSE,
          true);
  private MagicLinkService magicLinkService;

  @BeforeEach
  void setUp() {
    var authParams = AuthParams.builder().projectId(PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.magicLinkService =
        (MagicLinkService) AuthenticationServiceBuilder.buildService(client, authParams);
  }

  @Test
  void signUp() {
    User user = new User("Some Name", "kuduwa.keshavram@gmail.com", "+917276787724");
    String signUp =
        magicLinkService.signUp(
            DeliveryMethod.EMAIL, "kuduwa.keshavram@gmail.com", "https://www.domain.com", user);
    Assertions.assertThat(signUp).isNotBlank().contains("*");
  }

  @SneakyThrows
  @Test
  void verify() {
    ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
    var authenticationInfo =
        magicLinkService.verify("de452731b4afca0553082d62f7a37e11ddb79bb410b25d45ee80b83c0eacafa4");
    Assertions.assertThat(authenticationInfo).isNotNull();

    Token sessionToken = authenticationInfo.getToken();
    Assertions.assertThat(sessionToken).isNotNull();
    Assertions.assertThat(sessionToken.getJwt()).isNotBlank();
    Assertions.assertThat(sessionToken.getClaims()).isNotEmpty();
    Assertions.assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);

    Token refreshToken = authenticationInfo.getRefreshToken();
    Assertions.assertThat(refreshToken).isNotNull();
    Assertions.assertThat(refreshToken.getJwt()).isNotBlank();
    Assertions.assertThat(refreshToken.getClaims()).isNotEmpty();
    Assertions.assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);

    UserResponse user = authenticationInfo.getUser();
    Assertions.assertThat(user).isNotNull();
    Assertions.assertThat(user.getUserId()).isNotBlank();
    Assertions.assertThat(user.getLoginIds()).isNotEmpty();
  }

  @Test
  void signIn() {
    String signIn =
        magicLinkService.signIn(
            DeliveryMethod.EMAIL, "kuduwa.keshavram@gmail.com", "https://www.domain.com");
    Assertions.assertThat(signIn).isNotBlank().contains("*");
  }
}
