package com.descope.model.agentic;

/** OAuth grant types an access request can ask for. */
public final class GovernanceGrantType {
  public static final String AUTH_CODE = "authCode";
  public static final String CLIENT_CREDENTIALS = "clientCredentials";
  public static final String JWT_BEARER = "jwtBearer";
  public static final String CIBA = "ciba";

  private GovernanceGrantType() {
  }
}
