package com.descope.model.auth;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDPResponse {
  private List<String> idpGroups;
  private Map<String, Object> idpSAMLAttributes;
  private Map<String, Object> idpOIDCClaims;
}
