package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.permission.PermissionResponse;

public interface PermissionService {
    void create(String name, String description) throws DescopeException;

    void update(String name, String newName, String description) throws DescopeException;

    void delete(String name) throws DescopeException;

    PermissionResponse loadAll() throws DescopeException;
}
