package com.descope.model.fga;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGACheckRequest {
  // Set either tuples or checkTuples, not both
  private List<FGARelation> tuples;
  private List<FGACheckTuple> checkTuples;
  // Context for condition evaluation, applies to tuples without their own
  private Map<String, Object> context;
  // Include the path of relations that allowed each check
  private Boolean computePaths;
  // Include the IDs of the conditions each check evaluated
  private Boolean listConditions;
}
