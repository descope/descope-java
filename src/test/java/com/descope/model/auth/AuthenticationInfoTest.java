package com.descope.model.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import org.junit.jupiter.api.Test;

class AuthenticationInfoTest {

  @Test
  void testFromJwtResponseCopiesAllFields() {
    JWTResponse jwtResponse = new JWTResponse();
    jwtResponse.setFirstSeen(true);
    jwtResponse.setIdpResponse(new IDPResponse());
    jwtResponse.setSessionExpiration(11);
    jwtResponse.setExternalToken("et");
    jwtResponse.setTenantSSOID("ts");
    jwtResponse.setTrustedDeviceJwt("td");
    jwtResponse.setNextRefreshSeconds(12);
    jwtResponse.setCookieName("cn");
    jwtResponse.setSessionCookieName("scn");
    jwtResponse.setSessionCookieDomain("scd");
    jwtResponse.setUnsavedSSO(true);
    jwtResponse.setKnownDeviceJwt("kd");
    jwtResponse.setSsoAuthenticationOnly(true);
    jwtResponse.setUserCreationDeferred(true);
    Token token = Token.builder().jwt("s").build();
    Token refresh = Token.builder().jwt("r").build();

    AuthenticationInfo info = AuthenticationInfo.fromJwtResponse(token, refresh, jwtResponse);

    assertThat(info.getToken()).isSameAs(token);
    assertThat(info.getRefreshToken()).isSameAs(refresh);
    assertThat(info.getFirstSeen()).isTrue();
    assertThat(info.getIdpResponse()).isSameAs(jwtResponse.getIdpResponse());
    assertThat(info.getSessionExpiration()).isEqualTo(11);
    assertThat(info.getExternalToken()).isEqualTo("et");
    assertThat(info.getTenantSSOID()).isEqualTo("ts");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("td");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(12);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kd");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();
  }
}
