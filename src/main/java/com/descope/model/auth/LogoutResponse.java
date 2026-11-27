package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {
  /**
   * Set by a logout with ssoLogout when the session signed in through a SAML SSO configuration with single logout on:
   * send the browser here to log the user out of the IdP too. Empty when there is nothing to do at the IdP.
   */
  private String ssoLogoutUrl;
}
