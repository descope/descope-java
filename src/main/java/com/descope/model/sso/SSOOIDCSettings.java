package com.descope.model.sso;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOOIDCSettings {
  private String name;
  private String clientId;
  private String clientSecret;
  private String redirectUrl;
  private String authUrl;
  private String tokenUrl;
  private String userDataUrl;
  private List<String> scope;
  @JsonProperty("JWKsUrl")
  private String jwksUrl;
  private OIDCAttributeMapping userAttrMapping;
  private Boolean manageProviderTokens;
  private String callbackDomain;
  private List<String> prompt;
  private String grantType;
  private String issuer;
}
