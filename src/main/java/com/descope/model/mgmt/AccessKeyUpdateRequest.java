package com.descope.model.mgmt;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AccessKeyUpdateRequest {
  private String id;
  private String name;
  private String description;
  private List<String> roleNames;
  private List<Map<String, Object>> keyTenants;
  private Map<String, Object> customClaims;
  private List<String> permittedIps;
}
