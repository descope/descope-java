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
public class FGACheckResponse {
  private List<FGACheckResponseTuple> tuples;
  // Version of the schema the condition IDs in the results refer to
  private String schemaVersion;

  public FGACheckResponse(List<FGACheckResponseTuple> tuples) {
    this.tuples = tuples;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FGACheckResponseTuple {
    private boolean allowed;
    private FGARelation tuple;
    private FGACheckInfo info;
  }
}
