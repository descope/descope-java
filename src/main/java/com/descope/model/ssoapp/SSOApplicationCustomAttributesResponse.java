package com.descope.model.ssoapp;

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
public class SSOApplicationCustomAttributesResponse {
  private List<CustomAttribute> data;
  private Long total;
}
