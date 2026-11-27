package com.descope.model.fga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGACheckOptions {
  // Return the path of relations that granted each allowed check
  private Boolean computePaths;
  // Return the IDs of the schema conditions that evaluated true or false
  private Boolean listConditions;
}
