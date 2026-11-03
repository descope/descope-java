package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// A per-field filter for searching governance access requests.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestSearchField {
  private String field;
  private List<String> valArr;
  private String valStr;
  private boolean negative;
  private String valType;
}
