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
public class SSOSAMLSettingsResponse {
  private String idpEntityId;
  private String idpSSOUrl;
  private String idpCertificate;
  private String idpMetadataUrl;
  private String spEntityId;
  private String spACSUrl;
  private String spCertificate;
  private AttributeMapping attributeMapping;
  private List<GroupsMapping> groupsMapping;
  private String redirectUrl;
  private String spSignCertificate;
  private String subjectNameIdFormat;
}
