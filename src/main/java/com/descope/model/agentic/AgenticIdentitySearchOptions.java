package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Filters for searching agentic identities. All fields are optional.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentitySearchOptions {
  private String userId;
  private String clientId;
  private String resourceId;
  private String consentId;
  // Page number, starting with 0 for the first page
  private int page;
  private int limit;
}
