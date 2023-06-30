package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.mgmt.AccessKeyResponse;
import java.util.List;

public interface AccessKeyService {

  AccessKeyResponse create(String name, int expireTime, List<String> roleNames,
      List<AssociatedTenant> keyTenants) throws DescopeException;

  AccessKeyResponse load(String id) throws DescopeException;

  AccessKeyResponse searchAll(List<String> tenantIDs) throws DescopeException;

  AccessKeyResponse update(String id, String name) throws DescopeException;

  AccessKeyResponse deactivate(String id) throws DescopeException;

  AccessKeyResponse activate(String id) throws DescopeException;

  AccessKeyResponse delete(String id) throws DescopeException;
}
