package com.descope.model.mgmt;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessKeyRequest {
  private String name;
  private long expireTime;
  private List<String> roleNames;
  private List<Map<String, Object>> keyTenants;
  private String userId;
}
