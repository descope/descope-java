package com.descope.model.fga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGACheckResult {
  private boolean allowed;
  private FGARelation relation;
  private FGACheckInfo info;
}
