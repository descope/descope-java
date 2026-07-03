package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.mgmt.AccessKeyRequest;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseList;
import com.descope.model.mgmt.AccessKeyUpdateRequest;
import java.util.List;
import java.util.Map;

public interface AccessKeyService {

  AccessKeyResponse create(String name, int expireTime, List<String> roleNames, List<AssociatedTenant> keyTenants)
      throws DescopeException;

  AccessKeyResponse create(String name, int expireTime, List<String> roleNames, List<AssociatedTenant> keyTenants,
      Map<String, Object> customClaims) throws DescopeException;

  AccessKeyResponse create(String name, int expireTime, List<String> roleNames, List<AssociatedTenant> keyTenants,
      String userId) throws DescopeException;

  AccessKeyResponse create(String name, int expireTime, List<String> roleNames, List<AssociatedTenant> keyTenants,
      String userId, Map<String, Object> customClaims) throws DescopeException;

  AccessKeyResponse create(AccessKeyRequest req) throws DescopeException;

  AccessKeyResponse load(String id) throws DescopeException;

  AccessKeyResponseList searchAll(List<String> tenantIDs) throws DescopeException;

  AccessKeyResponse update(String id, String name) throws DescopeException;

  AccessKeyResponse update(AccessKeyUpdateRequest req) throws DescopeException;

  AccessKeyResponse deactivate(String id) throws DescopeException;

  AccessKeyResponse activate(String id) throws DescopeException;

  void delete(String id) throws DescopeException;

  /**
   * Rotate an access key, invalidating the current cleartext and generating a new one.
   *
   * @param id - The access key ID to rotate
   * @return {@link AccessKeyResponse} containing the updated details and the new cleartext
   * @throws DescopeException in case of errors
   */
  AccessKeyResponse rotate(String id) throws DescopeException;

  /**
   * Deactivate multiple access keys in a single request.
   *
   * @param ids - The list of access key IDs to deactivate
   * @throws DescopeException in case of errors
   */
  void deactivateBatch(List<String> ids) throws DescopeException;

  /**
   * Activate multiple access keys in a single request.
   *
   * @param ids - The list of access key IDs to activate
   * @throws DescopeException in case of errors
   */
  void activateBatch(List<String> ids) throws DescopeException;

  /**
   * Delete multiple access keys in a single request.
   *
   * @param ids - The list of access key IDs to delete
   * @throws DescopeException in case of errors
   */
  void deleteBatch(List<String> ids) throws DescopeException;
}
