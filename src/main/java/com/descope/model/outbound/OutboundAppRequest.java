package com.descope.model.outbound;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundAppRequest {
  private String id;
  private String name;
  private String description;
  private String clientId;
  private String clientSecret;
  private String logo;
  private String discoveryUrl;
  private String authorizationUrl;
  private List<OutboundAppURLParam> authorizationUrlParams;
  private String tokenUrl;
  private List<OutboundAppURLParam> tokenUrlParams;
  private String revocationUrl;
  private List<String> defaultScopes;
  private String defaultRedirectUrl;
  private String callbackDomain;
  private Boolean pkce;
  // See OutboundAppConstants.ACCESS_TYPE_*
  private String accessType;
  // See OutboundAppConstants.PROMPT_*
  private List<String> prompt;
  // See OutboundAppConstants.APP_TYPE_*
  private String appType;
  private Boolean useDcr;
  private String dcrUrl;
  private String tenantId;
  // See OutboundAppConstants.TOKEN_ENDPOINT_AUTH_METHOD_*
  private String tokenEndpointAuthMethod;
  // See OutboundAppConstants.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_*; used with private_key_jwt
  private String tokenEndpointAuthSigningAlg;
  private Boolean useNonce;

  public OutboundAppRequest(String id, String name) {
    this.id = id;
    this.name = name;
  }
}
