package com.descope.model.mgmt;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessKeySearchRequest {
  private List<String> tenantIds;
  private String text;
  private List<AccessKeySearchSort> sort;
  private String boundUserId;
  private String creatingUser;
  private Map<String, Object> customAttributes;
}
