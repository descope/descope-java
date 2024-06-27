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
public class SSOSAMLSettingsByMetadata {
  private String idpMetadataUrl;
  private AttributeMapping attributeMapping;
  private List<RoleMapping> roleMappings;
  private String spEncryptionKey;
  private String spSignKey;
  private String subjectNameIdFormat;
}
