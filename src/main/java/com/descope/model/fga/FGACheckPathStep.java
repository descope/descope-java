package com.descope.model.fga;

import com.descope.enums.FGACheckPathStepType;
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
  private FGACheckPathStepType stepType;
  private FGARelation tuple;
  private String permission;
  private List<FGACheckPath> subPaths;
}
