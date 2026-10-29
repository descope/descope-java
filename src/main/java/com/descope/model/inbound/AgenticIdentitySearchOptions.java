package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentitySearchOptions {
  private String userId;
  private String clientId;
  private String resourceId;
  private String consentId;
  private Integer page;
  private Integer limit;
}
