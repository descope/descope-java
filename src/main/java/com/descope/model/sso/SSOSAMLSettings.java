package com.descope.model.sso;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOSAMLSettings {
  private String idpUrl;
  private String entityId;
  private String idpCert;
  private AttributeMapping attributeMapping;
  private List<RoleMapping> roleMappings;
}
