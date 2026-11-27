package com.descope.model.fga;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGACheckTuple {
  private FGARelation tuple;
  // Overrides the request-level context for this tuple
  private Map<String, Object> context;
}
