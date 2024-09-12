package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplicationOIDCSettings {
  private String loginPageUrl;
  private String issuer;
  private String discoveryUrl;
  private Boolean forceAuthentication;
}
