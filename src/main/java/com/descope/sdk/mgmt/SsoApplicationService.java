package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.ssoapp.OIDCApplicationRequest;
import com.descope.model.ssoapp.SAMLApplicationRequest;
import com.descope.model.ssoapp.SSOApplication;
import java.util.List;

public interface SsoApplicationService {

  /**
   * Create a new OIDC SSO application with the given name.
   *
   * @param appRequest the OIDC application details
   * @return the new ID of the application
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  String createOIDCApplication(OIDCApplicationRequest appRequest) throws DescopeException;

  /**
   * Create a new SAML SSO application with the given name.
   *
   * @param appRequest the SAML application details
   * @return the new ID of the application
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  String createSAMLApplication(SAMLApplicationRequest appRequest) throws DescopeException;

  /**
   * Update an existing OIDC sso application.
   * IMPORTANT: All parameters are required and will override whatever value is currently
   *
   * @param appRequest the application details.
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void updateOIDCApplication(OIDCApplicationRequest appRequest) throws DescopeException;

  /**
   * Update an existing SAML sso application.
   * IMPORTANT: All parameters are required and will override whatever value is currently
   *
   * @param appRequest the application details.
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void updateSAMLApplication(SAMLApplicationRequest appRequest) throws DescopeException;

  /**
   * Delete an existing sso application.
   * IMPORTANT: This action is irreversible. Use carefully.
   *
   * @param id application ID to delete
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void delete(String id) throws DescopeException;

  /**
   * Load project sso application by id.
   *
   * @param id ID of application to load
   * @return {@link SSOApplication} details
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOApplication load(String id) throws DescopeException;

  /**
   * Load all project sso applications.
   *
   * @return {@link SSOApplication} details
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  List<SSOApplication> loadAll() throws DescopeException;
}
