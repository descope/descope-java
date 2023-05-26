package com.descope.model.mgmt;

import com.descope.model.auth.AssociatedTenant;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccessKeyResponse {

  private String id;
  private String name;
  private List<String> roleNames;
  private List<AssociatedTenant> keyTenants;
  private String status;
  private long createdTime;
  private long expireTime;
  private String createdBy;
  
}

