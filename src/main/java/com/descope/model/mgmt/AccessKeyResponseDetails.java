package com.descope.model.mgmt;

import com.descope.model.auth.AssociatedTenant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessKeyResponseDetails {
  private String id;
  private String name;
  private String description;
  private List<String> roleNames;
  private List<AssociatedTenant> keyTenants;
  private String status;
  private long createdTime;
  private long expireTime;
  private String createdBy;
  private String clientId;
  private String userId;
  private List<String> permittedIps;
  private Map<String, Object> customAttributes;

  // Keeps the pre-customAttributes constructor binary compatible
  public AccessKeyResponseDetails(String id, String name, String description, List<String> roleNames,
      List<AssociatedTenant> keyTenants, String status, long createdTime, long expireTime, String createdBy,
      String clientId, String userId, List<String> permittedIps) {
    this(id, name, description, roleNames, keyTenants, status, createdTime, expireTime, createdBy, clientId,
        userId, permittedIps, null);
  }
}
