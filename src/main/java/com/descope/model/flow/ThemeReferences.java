package com.descope.model.flow;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeReferences {
  private Map<String, String> connectors;
  private List<String> oauth;
  private List<String> omitted;
  private Map<String, String> roles;
  private List<String> userAttributes;
  private List<String> tenantAttributes;
}
