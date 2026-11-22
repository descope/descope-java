package com.descope.model.customattributes;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomAttributesResponse {
  private List<CustomAttribute> data;
  // int64 in the API; int suffices, as a project never has 2^31 custom attributes
  private int total;
}
