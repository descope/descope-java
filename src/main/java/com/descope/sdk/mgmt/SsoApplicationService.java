package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.model.ssoapp.OIDCApplicationRequest;
import com.descope.model.ssoapp.SAMLApplicationRequest;
import com.descope.model.ssoapp.SSOApplication;
import com.descope.model.ssoapp.SSOApplicationAddSecretResponse;
import com.descope.model.ssoapp.SSOApplicationCustomAttributesResponse;
import com.descope.model.ssoapp.SSOApplicationSecretMeta;
import com.descope.model.ssoapp.WSFedApplicationRequest;
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
   * Create a new WS-Federation SSO application with the given name.
   *
   * @param appRequest the WS-Federation application details
   * @return the new ID of the application
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  String createWSFedApplication(WSFedApplicationRequest appRequest) throws DescopeException;

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
   * Update an existing WS-Federation sso application.
   * IMPORTANT: All parameters are required and will override whatever value is currently
   *
   * @param appRequest the application details.
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void updateWSFedApplication(WSFedApplicationRequest appRequest) throws DescopeException;

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

  /**
   * Get the cleartext secret of an sso application by id.
   *
   * @param id ID of the application to get the secret for
   * @return the cleartext application secret
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  String getApplicationSecret(String id) throws DescopeException;

  /**
   * Get the cleartext of a named secret of an sso application.
   *
   * @param id ID of the application to get the secret for
   * @param secretName optional name of the secret; {@code null} or empty gets the application's secret
   * @return the cleartext application secret
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  String getApplicationSecret(String id, String secretName) throws DescopeException;

  /**
   * Rotate the secret of an sso application by id and return the new cleartext secret.
   *
   * @param id ID of the application to rotate the secret for
   * @return the rotated cleartext application secret
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  String rotateApplicationSecret(String id) throws DescopeException;

  /**
   * Add a named secret to an sso application.
   *
   * @param id ID of the application to add the secret to
   * @param name required name of the secret
   * @param expireTime optional expiration time in epoch seconds; {@code null} or 0 means it never expires
   * @return {@link SSOApplicationAddSecretResponse} with the secret's metadata and its cleartext
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOApplicationAddSecretResponse addApplicationSecret(String id, String name, Long expireTime)
      throws DescopeException;

  /**
   * Revoke a named secret of an sso application.
   *
   * @param id ID of the application to revoke the secret of
   * @param name required name of the secret to revoke
   * @return the metadata of the application's remaining secrets
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  List<SSOApplicationSecretMeta> revokeApplicationSecret(String id, String name) throws DescopeException;

  /**
   * Get all sso application custom attributes.
   *
   * @return {@link SSOApplicationCustomAttributesResponse}
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOApplicationCustomAttributesResponse getCustomAttributes() throws DescopeException;

  /**
   * Create sso application custom attributes.
   *
   * @param request the custom attributes (1 or more) to create
   * @return {@link SSOApplicationCustomAttributesResponse}
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOApplicationCustomAttributesResponse createCustomAttributes(CreateCustomAttributesRequest request)
      throws DescopeException;

  /**
   * Delete sso application custom attributes.
   *
   * @param request the names of the attributes to delete
   * @return {@link SSOApplicationCustomAttributesResponse}
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOApplicationCustomAttributesResponse deleteCustomAttributes(DeleteCustomAttributesRequest request)
      throws DescopeException;
}
