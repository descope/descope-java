package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.roles.RoleResponse;
import java.util.List;

public interface RolesService {

  void create(String name, String description, List<String> permissionNames)
      throws DescopeException;

  void update(String name, String newName, String description, List<String> permissionNames)
      throws DescopeException;

  void delete(String name) throws DescopeException;

  RoleResponse loadAll() throws DescopeException;
}
