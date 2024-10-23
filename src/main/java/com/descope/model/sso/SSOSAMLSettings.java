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
  private String spEncryptionKey;
  private String spSignKey;
  private String subjectNameIdFormat;
  // NOTICE - the following fields should be overridden only in case of SSO migration,
  //          otherwise, do not modify these fields
  private String spACSUrl;
  private String spEntityId;
}
