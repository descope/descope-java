package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentitySearchRequest {
  private String userId;
  private String clientId;
  private String resourceId;
  private String consentId;
  // Page number starting with 0 for the first page
  private Integer page;
  private Integer limit;
}
