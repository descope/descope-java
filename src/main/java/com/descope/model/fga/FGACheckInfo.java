package com.descope.model.fga;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGACheckInfo {
  private boolean direct;
  private boolean conditional;
  private List<String> missingContext;
  private String conditionalErr;
  private boolean factUsed;
  // Set when the check was allowed and computePaths was requested
  private FGACheckPath path;
  // Set when the tuple is invalid
  private FGACheckError error;
  // Condition IDs (see FGACondition.getId()) that evaluated true, set when listConditions was requested
  private List<Integer> trueConditions;
  // Condition IDs (see FGACondition.getId()) that evaluated false, set when listConditions was requested
  private List<Integer> falseConditions;

  public FGACheckInfo(boolean direct, boolean conditional, List<String> missingContext, String conditionalErr,
      boolean factUsed) {
    this.direct = direct;
    this.conditional = conditional;
    this.missingContext = missingContext;
    this.conditionalErr = conditionalErr;
    this.factUsed = factUsed;
  }
}
