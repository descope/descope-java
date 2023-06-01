package com.descope.utils;

import com.descope.model.auth.AssociatedTenant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MgmtUtils {

  public static List<Map<String, Object>> createAssociatedTenantList(
      List<AssociatedTenant> tenants) {
    List<Map<String, Object>> associatedTenantList = new ArrayList<>();
    for (AssociatedTenant tenant : tenants) {
      Map<String, Object> map = new HashMap<>();
      map.put("tenantId", tenant.getTenantId());
      map.put("roleNames", tenant.getRoleNames());
      associatedTenantList.add(map);
    }
    return associatedTenantList;
  }
}
