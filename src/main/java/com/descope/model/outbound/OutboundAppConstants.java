package com.descope.model.outbound;

import lombok.experimental.UtilityClass;

// Known string values of outbound application fields; the server may add more.
@UtilityClass
public class OutboundAppConstants {
  // accessType
  public static final String ACCESS_TYPE_OFFLINE = "offline";
  public static final String ACCESS_TYPE_ONLINE = "online";

  // prompt
  public static final String PROMPT_NONE = "none";
  public static final String PROMPT_LOGIN = "login";
  public static final String PROMPT_CONSENT = "consent";
  public static final String PROMPT_SELECT_ACCOUNT = "select_account";

  // appType
  public static final String APP_TYPE_OAUTH = "oauth";
  public static final String APP_TYPE_API_KEY = "apikey";

  // tokenEndpointAuthMethod
  public static final String TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET = "client_secret";
  public static final String TOKEN_ENDPOINT_AUTH_METHOD_PRIVATE_KEY_JWT = "private_key_jwt";

  // tokenEndpointAuthSigningAlg
  public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG_RS256 = "RS256";
  public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG_RS512 = "RS512";
}
