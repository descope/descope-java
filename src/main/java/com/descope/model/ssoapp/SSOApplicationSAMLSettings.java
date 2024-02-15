package com.descope.model.ssoapp;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplicationSAMLSettings {
  private String loginPageUrl;
  private String idpCert;
  private Boolean useMetadataInfo;
  private String metadataUrl;
  private String entityId;
  private String acsUrl;
  private String certificate;
  private List<SAMLIDPAttributeMappingInfo> attributeMapping;
  private List<SAMLIDPGroupsMappingInfo> groupsMapping;
  private String idpMetadataUrl;
  private String idpEntityId;
  private String idpSsoUrl;
  private List<String> acsAllowedCallbacks;
  private String subjectNameIdType;
  private String subjectNameIdFormat;
}
