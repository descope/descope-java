package com.descope.sdk.auth;

import com.descope.enums.PromptType;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.SAMLLoginOptions;
import com.descope.model.magiclink.LoginOptions;
import java.util.List;

public interface SAMLService {
  /**
   * tart will initiate a SAML login flow.
   *
   * @param tenant       - tenant
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return will be the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String returnURL, LoginOptions loginOptions) throws DescopeException;

  /**
   * Start will initiate a SAML login flow, with the optional authorization parameters.
   *
   * @param tenant         - tenant
   * @param returnURL      - return url
   * @param loginOptions   - {@link LoginOptions loginOptions}, or {@link SAMLLoginOptions} to also send
   *                       the SSO application ID, PKCE challenge or tenant ID
   * @param refreshToken   - the user's refresh JWT, required when the login options set stepup or mfa
   * @param prompt         - optional prompts to the user, overriding the configuration
   * @param forceAuthn     - require the identity provider to authenticate the user again
   * @param loginHint      - optional login hint for the identity provider
   * @param initiatedEmail - optional email of the user who initiated the login
   * @return will be the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String returnURL, LoginOptions loginOptions, String refreshToken,
      List<PromptType> prompt, boolean forceAuthn, String loginHint, String initiatedEmail)
      throws DescopeException;

  /**
   * ExchangeToken - Finalize SAML authentication.
   *
   * @param code - Code to be validated
   * @return Authentication info. When {@code getSsoAuthenticationOnly()} is true, no session
   *     was issued and the token is null: do not treat the result as a login.
   * @throws DescopeException - error upon failure
   */

  AuthenticationInfo exchangeToken(String code) throws DescopeException;

}
