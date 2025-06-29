package com.descope.model.fga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGAResourceIdentifier {
  private String resourceId;
  private String resourceType;
}
