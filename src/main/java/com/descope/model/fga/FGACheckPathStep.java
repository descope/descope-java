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
public class FGACheckPathStep {
  public static final String STEP_TYPE_UNKNOWN = "UNKNOWN";
  public static final String STEP_TYPE_RELATION = "RELATION";
  public static final String STEP_TYPE_INTERSECTION = "INTERSECTION";

  private String stepType;
  private FGARelation tuple;
  private String permission;
  private List<FGACheckPath> subPaths;
}
