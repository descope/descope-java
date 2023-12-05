package com.descope.utils;

import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.model.auth.AssociatedTenant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MgmtUtils {

  public static List<Map<String, Object>> createAssociatedTenantList(
      List<AssociatedTenant> tenants) {
    if (tenants == null) {
      return null;
    }
    List<Map<String, Object>> associatedTenantList = new ArrayList<>();
    for (AssociatedTenant tenant : tenants) {
      Map<String, Object> map = mapOf("tenantId", tenant.getTenantId(), "roleNames", tenant.getRoleNames());
      associatedTenantList.add(map);
    }
    return associatedTenantList;
  }
}
