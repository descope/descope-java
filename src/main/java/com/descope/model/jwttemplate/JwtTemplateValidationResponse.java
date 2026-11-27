package com.descope.model.jwttemplate;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTemplateValidationResponse {
  private Boolean valid;
  private List<JwtTemplateValidationIssue> issues;
}
