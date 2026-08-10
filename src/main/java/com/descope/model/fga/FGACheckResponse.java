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
