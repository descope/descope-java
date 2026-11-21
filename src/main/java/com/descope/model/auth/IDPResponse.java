package com.descope.model.auth;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IDPResponse {
  private List<String> idpGroups;
  private Map<String, Object> idpSAMLAttributes;
  private Map<String, Object> idpOIDCClaims;
  private String samlResponse;
  private String samlGeneratedUser;
  private String samlGeneratedRoles;
  private String oidcResponse;
  private String oidcGeneratedUser;
  private String oidcGeneratedRoles;

  public IDPResponse(List<String> idpGroups, Map<String, Object> idpSAMLAttributes,
      Map<String, Object> idpOIDCClaims) {
    this.idpGroups = idpGroups;
    this.idpSAMLAttributes = idpSAMLAttributes;
    this.idpOIDCClaims = idpOIDCClaims;
  }
}
