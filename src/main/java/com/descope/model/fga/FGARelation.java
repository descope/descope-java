package com.descope.model.fga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGARelation {
  private String resource;
  private String resourceType;
  private String relation;
  private String target;
  private String targetType;
}
