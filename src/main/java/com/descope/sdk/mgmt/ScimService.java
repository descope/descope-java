package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.scim.ActivateScimAccessKeysRequest;
import com.descope.model.scim.ActivateScimAccessKeysResponse;
import com.descope.model.scim.CreateScimAccessKeyRequest;
import com.descope.model.scim.LoadScimAccessKeysResponse;
import com.descope.model.scim.RevokeScimAccessKeysRequest;
import com.descope.model.scim.RevokeScimAccessKeysResponse;
import com.descope.model.scim.RotateScimAccessKeyRequest;
import com.descope.model.scim.ScimAccessKeyResponse;

/** Provides functions for managing the SCIM access keys of tenants. */
public interface ScimService {

  /**
   * Load the SCIM access keys of a tenant. Returns key metadata only, never the cleartext.
   *
   * @param tenantId the tenant ID, required
   * @param ssoId the SSO configuration ID, or {@code null} for the tenant's keys across every SSO configuration
   * @return {@link LoadScimAccessKeysResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  LoadScimAccessKeysResponse loadKeys(String tenantId, String ssoId) throws DescopeException;

  /**
   * Create a SCIM access key for a tenant. Existing SCIM keys for the same SSO configuration stay valid.
   *
   * @param request the key to create, {@code tenantId} is required
   * @return {@link ScimAccessKeyResponse} with the cleartext bearer token, returned exactly once
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ScimAccessKeyResponse createKey(CreateScimAccessKeyRequest request) throws DescopeException;

  /**
   * Rotate the SCIM access keys of a tenant: create a new key and revoke the existing ones of the same SSO
   * configuration. SCIM provisioning fails until the identity provider is updated with the new bearer token.
   *
   * @param request the key to create, {@code tenantId} is required
   * @return {@link ScimAccessKeyResponse} with the cleartext bearer token, returned exactly once
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ScimAccessKeyResponse rotateKey(RotateScimAccessKeyRequest request) throws DescopeException;

  /**
   * Revoke every SCIM access key of a tenant for the given SSO configuration. This disables SCIM provisioning for
   * that configuration until a new key is created.
   *
   * @param request the keys to revoke, {@code tenantId} is required
   * @return {@link RevokeScimAccessKeysResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  RevokeScimAccessKeysResponse revokeKeys(RevokeScimAccessKeysRequest request) throws DescopeException;

  /**
   * Re-enable SCIM access keys that were revoked with {@link com.descope.model.scim.ScimKeyRevokeMode#DEACTIVATE}.
   *
   * @param request {@code tenantId} and {@code keyIds} (1 to 100) are required
   * @return {@link ActivateScimAccessKeysResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ActivateScimAccessKeysResponse activateKeys(ActivateScimAccessKeysRequest request) throws DescopeException;
}
