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
public class SSOApplicationWSFedSettings {
  private String loginPageUrl;
  private String realm;
  private String replyUrl;
  private List<SAMLIDPAttributeMappingInfo> attributeMapping;
  private List<SAMLIDPGroupsMappingInfo> groupsMapping;
  private Boolean forceAuthentication;
  private String logoutRedirectUrl;
  private String errorRedirectUrl;
  private String idpCert;
  private String idpInitiatedUrl;
  private String idpMetadataUrl;
  private String idpEntityId;
  private String idpSsoUrl;
  private String idpLogoutUrl;
  private List<String> replyAllowedCallbacks;
}
