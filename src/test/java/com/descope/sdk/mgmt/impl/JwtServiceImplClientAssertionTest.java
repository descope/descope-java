package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.request.ClientAssertionRequest;
import com.descope.model.mgmt.ManagementServices;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtServiceImplClientAssertionTest {

  private JwtService jwtService;
  private RSAPrivateKey rsaPrivateKey;
  private RSAPublicKey rsaPublicKey;
  private ECPrivateKey ecPrivateKey;

  @BeforeEach
  void setUp() throws Exception {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.jwtService = mgmtServices.getJwtService();

    KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
    rsaGenerator.initialize(2048);
    KeyPair rsaKeyPair = rsaGenerator.generateKeyPair();
    this.rsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();
    this.rsaPublicKey = (RSAPublicKey) rsaKeyPair.getPublic();

    KeyPairGenerator ecGenerator = KeyPairGenerator.getInstance("EC");
    ecGenerator.initialize(256);
    KeyPair ecKeyPair = ecGenerator.generateKeyPair();
    this.ecPrivateKey = (ECPrivateKey) ecKeyPair.getPrivate();
  }

  @Test
  void testCreateClientAssertionWithRS256() throws Exception {
    String clientId = "test-client-id";
    String tokenEndpoint = "https://auth.example.com/oauth/token";

    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId(clientId)
        .tokenEndpoint(tokenEndpoint)
        .privateKey(rsaPrivateKey)
        .algorithm("RS256")
        .expirationSeconds(300)
        .build();

    String jwt = jwtService.createClientAssertion(request);

    assertNotNull(jwt);
    assertTrue(jwt.split("\\.").length == 3);

    Claims claims = Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    assertEquals(clientId, claims.getIssuer());
    assertEquals(clientId, claims.getSubject());
    assertEquals(tokenEndpoint, claims.getAudience().iterator().next());
    assertNotNull(claims.getId());
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());

    long expirationDiff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
    assertEquals(300000, expirationDiff, 1000);
  }

  @Test
  void testCreateClientAssertionWithES256() throws Exception {
    String clientId = "test-client-id";
    String tokenEndpoint = "https://auth.example.com/oauth/token";

    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId(clientId)
        .tokenEndpoint(tokenEndpoint)
        .privateKey(ecPrivateKey)
        .algorithm("ES256")
        .expirationSeconds(300)
        .build();

    String jwt = jwtService.createClientAssertion(request);

    assertNotNull(jwt);
    assertTrue(jwt.split("\\.").length == 3);
  }

  @Test
  void testCreateClientAssertionWithDefaultAlgorithm() throws Exception {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(rsaPrivateKey)
        .build();

    String jwt = jwtService.createClientAssertion(request);

    assertNotNull(jwt);

    Claims claims = Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    assertNotNull(claims);
  }

  @Test
  void testCreateClientAssertionWithCustomExpiration() throws Exception {
    long customExpiration = 600;
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(rsaPrivateKey)
        .expirationSeconds(customExpiration)
        .build();

    String jwt = jwtService.createClientAssertion(request);

    Claims claims = Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    long expirationDiff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
    assertEquals(600000, expirationDiff, 1000);
  }

  @Test
  void testCreateClientAssertionExpirationNotInPast() throws Exception {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(rsaPrivateKey)
        .build();

    String jwt = jwtService.createClientAssertion(request);

    Claims claims = Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    assertTrue(claims.getExpiration().after(new Date()));
  }

  @Test
  void testCreateClientAssertionUniqueJti() throws Exception {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(rsaPrivateKey)
        .build();

    String jwt1 = jwtService.createClientAssertion(request);
    String jwt2 = jwtService.createClientAssertion(request);

    Claims claims1 = Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseSignedClaims(jwt1)
        .getPayload();

    Claims claims2 = Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseSignedClaims(jwt2)
        .getPayload();

    assertNotNull(claims1.getId());
    assertNotNull(claims2.getId());
    assertTrue(!claims1.getId().equals(claims2.getId()));
  }

  @Test
  void testCreateClientAssertionNullRequest() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.createClientAssertion(null));

    assertEquals("The ClientAssertionRequest argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateClientAssertionEmptyClientId() {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(rsaPrivateKey)
        .build();

    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.createClientAssertion(request));

    assertEquals("The clientId argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateClientAssertionEmptyTokenEndpoint() {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("")
        .privateKey(rsaPrivateKey)
        .build();

    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.createClientAssertion(request));

    assertEquals("The tokenEndpoint argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateClientAssertionNullPrivateKey() {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(null)
        .build();

    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.createClientAssertion(request));

    assertEquals("The privateKey argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateClientAssertionUnsupportedAlgorithm() {
    ClientAssertionRequest request = ClientAssertionRequest.builder()
        .clientId("test-client")
        .tokenEndpoint("https://auth.example.com/token")
        .privateKey(rsaPrivateKey)
        .algorithm("INVALID_ALGORITHM")
        .build();

    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.createClientAssertion(request));

    assertNotNull(thrown);
    String message = thrown.getMessage();
    assertTrue(message != null && message.contains("algorithm"), 
        "Expected error message to contain 'algorithm', but got: " + message);
  }
}
