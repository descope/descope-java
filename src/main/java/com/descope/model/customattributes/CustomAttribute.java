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
public class CustomAttribute {
  private String name;
  private int type;
  private List<CustomAttributeOption> options;
  private String DisplayName;
}
