package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceSortField {
  private String field; // required
  private Boolean desc;
}
