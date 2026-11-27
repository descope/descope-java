package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutOptions {
  /**
   * Also log the user out of their SSO identity provider. When the session signed in through a SAML SSO
   * configuration with single logout on, the response's ssoLogoutUrl sends the browser to the IdP. The Descope
   * session ends either way.
   */
  private Boolean ssoLogout;
  /**
   * With ssoLogout, where the browser lands once the IdP has logged the user out. Must be in the project's approved
   * domains. Empty uses the SSO configuration's redirect URL.
   */
  private String redirectUrl;
}
