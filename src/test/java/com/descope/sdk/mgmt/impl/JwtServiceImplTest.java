package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.mgmt.JwtService;
import com.descope.sdk.mgmt.UserService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class JwtServiceImplTest {

  private final Map<String, Object> mockCustomClaims = mapOf("test", "claim");
  private JwtService jwtService;
  private UserService userService;
  private OTPService otpService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.jwtService = mgmtServices.getJwtService();
    this.userService = mgmtServices.getUserService();
    this.otpService = AuthenticationServiceBuilder.buildServices(client).getOtpService();
  }

  @Test
  void testupdateJWTWithCustomClaimsForEmptyJwt() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> jwtService.updateJWTWithCustomClaims("", mockCustomClaims));
    assertNotNull(thrown);
    assertEquals("The JWT argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateJWTWithCustomClaimsRefreshDuration() throws Exception {
    Map<?, ?> body = sentUpdateJwtBody(() -> jwtService.updateJWTWithCustomClaims("old-jwt", mockCustomClaims, 3));
    assertEquals("old-jwt", body.get("jwt"));
    assertEquals(mockCustomClaims, body.get("customClaims"));
    assertEquals(3, body.get("refreshDuration"));

    body = sentUpdateJwtBody(() -> jwtService.updateJWTWithCustomClaims("old-jwt", mockCustomClaims, null));
    assertFalse(body.containsKey("refreshDuration"));
    body = sentUpdateJwtBody(() -> jwtService.updateJWTWithCustomClaims("old-jwt", mockCustomClaims));
    assertFalse(body.containsKey("refreshDuration"));
  }

  @Test
  void testUpdateJWTWithCustomClaimsRefreshDurationForEmptyJwt() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> jwtService.updateJWTWithCustomClaims("", mockCustomClaims, 3));
    assertEquals("The JWT argument is invalid", thrown.getMessage());
  }

  // Runs update against a mocked HTTP client and returns the JSON body it sent.
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<?, ?> sentUpdateJwtBody(Supplier<Token> update) throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream("{\"jwt\":\"new-jwt\"}".getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      mockedJwtUtils.when(() -> JwtUtils.getToken(eq("new-jwt"), any())).thenReturn(TestUtils.MOCK_TOKEN);
      assertEquals(TestUtils.MOCK_TOKEN, update.get());
    }
    ClassicHttpRequest request = captor.getValue();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/jwt/update"));
    assertTrue(request.getHeader("Authorization").getValue().startsWith("Bearer " + TestUtils.PROJECT_ID));
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId, UserRequest.builder().email(loginId).verifiedEmail(true).build());
    OTPTestUserResponse code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    Assertions.assertThat(authInfo.getToken().getJwt()).isNotBlank();
    Token newJwt = jwtService.updateJWTWithCustomClaims(authInfo.getToken().getJwt(), mockCustomClaims);
    assertNotNull(newJwt.getClaims());
    assertEquals("claim", newJwt.getClaims().get("test"));
    userService.delete(loginId);

    AuthenticationInfo anonyUser = jwtService.anonymous(
            AnonymousUserRequest.builder().customClaims(mockCustomClaims).build());
    assertNotNull(anonyUser.getToken());
    assertNull(anonyUser.getUser());
  }
}
