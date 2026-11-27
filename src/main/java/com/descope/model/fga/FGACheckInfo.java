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
  // Set when the check was made with computePaths
  private FGACheckPath path;
  // Schema condition IDs, set when the check was made with listConditions
  private List<Integer> trueConditions;
  private List<Integer> falseConditions;
  // The schema version the check was evaluated against
  private String schemaVersion;

  public FGACheckInfo(boolean direct, boolean conditional, List<String> missingContext, String conditionalErr,
      boolean factUsed) {
    this.direct = direct;
    this.conditional = conditional;
    this.missingContext = missingContext;
    this.conditionalErr = conditionalErr;
    this.factUsed = factUsed;
  }
}
