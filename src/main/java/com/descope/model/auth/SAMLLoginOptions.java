package com.descope.model.auth;

import com.descope.model.magiclink.LoginOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Login options for SAML start, with the fields only that route accepts. */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SAMLLoginOptions extends LoginOptions {
  private String ssoAppId;
  // Base64-encoded PKCE challenge
  private String pkceChallenge;
  private String tenantId;

  // Not a request field
  @Override
  @JsonIgnore
  public boolean isJWTRequired() {
    return super.isJWTRequired();
  }
}
