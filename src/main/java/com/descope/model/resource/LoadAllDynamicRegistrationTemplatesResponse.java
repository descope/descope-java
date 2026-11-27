package com.descope.model.resource;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadAllDynamicRegistrationTemplatesResponse {
  private List<DynamicRegistrationTemplate> templates;
  private Integer total;
}
