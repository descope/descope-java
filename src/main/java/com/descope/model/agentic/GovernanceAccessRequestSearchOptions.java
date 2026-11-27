package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Options for searching access requests. All fields are optional. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestSearchOptions {
  /** Maximum number of requests to return. 0 returns all matches. */
  private Integer limit;
  /** 0-based page number, used with limit. */
  private Integer page;
  /** Per-field filters, combined with AND. */
  private List<SearchField> searchFields;
  /** Sort order. Defaults to newest first. */
  private List<SortField> sort;
  /** Free-text search across the request's text fields. */
  private String text;
}
