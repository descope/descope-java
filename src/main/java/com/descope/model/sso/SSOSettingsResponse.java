package com.descope.model.sso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOSettingsResponse {
  private String tenantID;
  private String idpEntityID;
  private String idpSSOURL;
  private String idpCertificate;
  private String idpMetadataURL;
  private String spEntityID;
  private String spACSUrl;
  private String spCertificate;
  private UserMapping userMapping;
  private List<GroupsMapping> groupsMapping;
  private String redirectURL;
  private String domain;
}

