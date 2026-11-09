package com.descope.model.mgmt;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Filters for searching access keys. All fields are optional. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessKeySearchRequest {
  private List<String> tenantIds;
  /** Free text search, up to 1000 characters. */
  private String text;
  private List<AccessKeySearchSort> sort;
  private String boundUserId;
  /** Up to 254 characters. */
  private String creatingUser;
  private Map<String, Object> customAttributes;
}
