package com.descope.model.auth;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.user.response.UserResponse;
import org.junit.jupiter.api.Test;

public class AuthenticationInfoTest {

  @Test
  void testFromCopiesJwtResponse() {
    Token token = Token.builder().jwt("s").build();
    Token refreshToken = Token.builder().jwt("r").build();
    UserResponse user = new UserResponse();
    IDPResponse idpResponse = new IDPResponse();
    JWTResponse jwtResponse = new JWTResponse();
    jwtResponse.setUser(user);
    jwtResponse.setFirstSeen(true);
    jwtResponse.setIdpResponse(idpResponse);
    jwtResponse.setCookieDomain("d.com");
    jwtResponse.setCookiePath("/p");
    jwtResponse.setCookieMaxAge(10);
    jwtResponse.setCookieExpiration(20);
    jwtResponse.setSessionExpiration(30);
    jwtResponse.setExternalToken("ext");
    jwtResponse.setClaims(mapOf("c", "v"));
    jwtResponse.setTenantSSOID("T1");
    jwtResponse.setTrustedDeviceJwt("tdj");
    jwtResponse.setNextRefreshSeconds(40);
    jwtResponse.setCookieName("DSR");
    jwtResponse.setSessionCookieName("DS");
    jwtResponse.setSessionCookieDomain("s.com");
    jwtResponse.setUnsavedSSO(true);
    jwtResponse.setKnownDeviceJwt("kdj");
    jwtResponse.setSsoAuthenticationOnly(true);
    jwtResponse.setUserCreationDeferred(true);

    AuthenticationInfo info = AuthenticationInfo.from(token, refreshToken, jwtResponse);

    assertSame(token, info.getToken());
    assertSame(refreshToken, info.getRefreshToken());
    assertSame(user, info.getUser());
    assertEquals(true, info.getFirstSeen());
    assertSame(idpResponse, info.getIdpResponse());
    assertEquals("d.com", info.getCookieDomain());
    assertEquals("/p", info.getCookiePath());
    assertEquals(10, info.getCookieMaxAge());
    assertEquals(20, info.getCookieExpiration());
    assertEquals(30, info.getSessionExpiration());
    assertEquals("ext", info.getExternalToken());
    assertEquals(mapOf("c", "v"), info.getClaims());
    assertEquals("T1", info.getTenantSSOID());
    assertEquals("tdj", info.getTrustedDeviceJwt());
    assertEquals(40, info.getNextRefreshSeconds());
    assertEquals("DSR", info.getCookieName());
    assertEquals("DS", info.getSessionCookieName());
    assertEquals("s.com", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("kdj", info.getKnownDeviceJwt());
    assertEquals(true, info.getSsoAuthenticationOnly());
    assertEquals(true, info.getUserCreationDeferred());
  }
}
