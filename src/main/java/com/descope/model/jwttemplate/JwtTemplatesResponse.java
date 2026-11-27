package com.descope.model.jwttemplate;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtTemplatesResponse {
  private List<JwtTemplate> templates;
}
