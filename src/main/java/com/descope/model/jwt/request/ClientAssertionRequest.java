package com.descope.model.jwt.request;

import java.security.PrivateKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for creating OAuth 2.0 client assertion JWT.
 * 
 * <p>This is used for client authentication using JWT bearer tokens as per RFC 7523.
 * The generated JWT can be used with OAuth token endpoints that support
 * client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAssertionRequest {
  /**
   * The client ID (issuer and subject of the JWT).
   */
  private String clientId;

  /**
   * The token endpoint URL (audience of the JWT).
   */
  private String tokenEndpoint;

  /**
   * The private key used to sign the JWT.
   * Typically an RSA or ECDSA private key.
   */
  private PrivateKey privateKey;

  /**
   * The signing algorithm to use (e.g., "RS256", "ES256").
   * Defaults to "RS256" if not specified.
   */
  @Builder.Default
  private String algorithm = "RS256";

  /**
   * JWT expiration time in seconds.
   * Defaults to 300 seconds (5 minutes) if not specified.
   */
  @Builder.Default
  private long expirationSeconds = 300;
}
