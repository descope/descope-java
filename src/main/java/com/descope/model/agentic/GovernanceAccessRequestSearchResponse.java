package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestSearchResponse {
  private List<GovernanceAccessRequest> requests;
  // Total number of matching requests, regardless of paging
  private int total;
}
