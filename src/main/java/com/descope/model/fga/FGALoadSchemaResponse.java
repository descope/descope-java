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
public class FGALoadSchemaResponse {
  private String dsl;
  private String version;
  private FGALoadSchemaConditions schema;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FGALoadSchemaConditions {
    private List<FGACondition> conditions;
  }
}
