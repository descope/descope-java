package com.descope.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.descope.exception.ClientFunctionalException;
import com.descope.model.auth.VerifyOptions;
import io.jsonwebtoken.Claims;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for audience verification in
 * {@link JwtUtils#verifyAudience(io.jsonwebtoken.Claims, VerifyOptions)}.
 *
 * <p>These tests exercise the new {@link VerifyOptions}-driven verification path in pure isolation
 * by mocking {@link Claims#getAudience()}, so no live signing key or Descope project is required.
 */
class JwtUtilsAudienceTest {

  private static Claims claimsWithAudience(String... audiences) {
    Claims claims = mock(Claims.class);
    Set<String> set = audiences.length == 0 ? Collections.emptySet()
        : new HashSet<>(Arrays.asList(audiences));
    when(claims.getAudience()).thenReturn(set);
    return claims;
  }

  private static Claims claimsWithNullAudience() {
    Claims claims = mock(Claims.class);
    when(claims.getAudience()).thenReturn(null);
    return claims;
  }

  @Test
  void nullOptionsSkipsVerification() {
    Claims claims = claimsWithAudience("anything");
    assertDoesNotThrow(() -> JwtUtils.verifyAudience(claims, null));
  }

  @Test
  void emptyAudiencesSkipsVerification() {
    Claims claims = claimsWithAudience("anything");
    VerifyOptions options = VerifyOptions.builder().audiences(Collections.emptyList()).build();
    assertDoesNotThrow(() -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void matchingSingleAudiencePasses() {
    Claims claims = claimsWithAudience("api.example.com");
    VerifyOptions options = VerifyOptions.withAudience("api.example.com");
    assertDoesNotThrow(() -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void mismatchingAudienceThrows() {
    Claims claims = claimsWithAudience("api.example.com");
    VerifyOptions options = VerifyOptions.withAudience("other.example.com");
    assertThrows(
        ClientFunctionalException.class, () -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void missingAudienceClaimThrows() {
    Claims claims = claimsWithAudience();
    VerifyOptions options = VerifyOptions.withAudience("api.example.com");
    assertThrows(
        ClientFunctionalException.class, () -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void nullAudienceClaimThrows() {
    Claims claims = claimsWithNullAudience();
    VerifyOptions options = VerifyOptions.withAudience("api.example.com");
    assertThrows(
        ClientFunctionalException.class, () -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void multipleExpectedAudiencesAnyMatchPasses() {
    Claims claims = claimsWithAudience("api.example.com");
    VerifyOptions options =
        VerifyOptions.withAudiences(Arrays.asList("other.example.com", "api.example.com"));
    assertDoesNotThrow(() -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void multipleTokenAudiencesAnyMatchPasses() {
    Claims claims = claimsWithAudience("first.example.com", "api.example.com");
    VerifyOptions options = VerifyOptions.withAudience("api.example.com");
    assertDoesNotThrow(() -> JwtUtils.verifyAudience(claims, options));
  }

  @Test
  void noneOfMultipleExpectedMatchThrows() {
    Claims claims = claimsWithAudience("first.example.com", "second.example.com");
    VerifyOptions options =
        VerifyOptions.withAudiences(Arrays.asList("third.example.com", "fourth.example.com"));
    assertThrows(
        ClientFunctionalException.class, () -> JwtUtils.verifyAudience(claims, options));
  }
}
