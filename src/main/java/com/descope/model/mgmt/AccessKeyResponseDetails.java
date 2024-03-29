package com.descope.model.mgmt;

import com.descope.model.auth.AssociatedTenant;
import java.util.List;
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
  private List<String> roleNames;
  private List<AssociatedTenant> keyTenants;
  private String status;
  private long createdTime;
  private long expireTime;
  private String createdBy;
  private String clientId;
  private String userId;
}
