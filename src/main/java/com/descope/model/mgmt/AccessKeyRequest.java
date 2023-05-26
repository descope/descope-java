package com.descope.model.mgmt;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AccessKeyRequest {

  private String name;
  private long expireTime;
  private List<String> roleNames;
  private List<Map<String, Object>> keyTenants;
}
