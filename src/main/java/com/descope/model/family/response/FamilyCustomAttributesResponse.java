package com.descope.model.family.response;

import com.descope.model.customattributes.CustomAttribute;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyCustomAttributesResponse {
  private List<CustomAttribute> data;
  private Long total;
}
