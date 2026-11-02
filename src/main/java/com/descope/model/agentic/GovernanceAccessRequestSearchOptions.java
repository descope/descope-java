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
public class GovernanceAccessRequestSearchOptions {
  // Maximum number of requests to return, 0 returns all matches
  private Integer limit;
  // 0-based page number, used with limit
  private Integer page;
  private List<GovernanceSearchField> searchFields;
  private List<GovernanceSortField> sort;
  // Free-text search across the request's text fields
  private String text;
}
