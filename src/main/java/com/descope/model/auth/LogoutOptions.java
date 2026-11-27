package com.descope.model.auth;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutOptions {
  private List<String> logoutType;
  /** Also log the user out of their SSO identity provider. The Descope session ends either way. */
  private Boolean ssoLogout;
  /**
   * With ssoLogout, where the browser lands once the IdP has logged the user out. Must be in the project's approved
   * domains. Empty uses the SSO configuration's redirect URL.
   */
  private String redirectUrl;
}
