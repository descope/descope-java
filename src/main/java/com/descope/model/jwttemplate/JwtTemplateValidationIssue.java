package com.descope.model.jwttemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTemplateValidationIssue {
  private String field;
  /** A stable identifier of the issue. */
  private String code;
  private String message;
  private String hint;
}
