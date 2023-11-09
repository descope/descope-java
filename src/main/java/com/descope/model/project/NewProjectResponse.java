package com.descope.model.project;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewProjectResponse {
  private String projectId;
  private String projectName;
  private Map<String, Object> projectSettingsWeb;
  private Map<String, Object> authMethodsMagicLink;
  private Map<String, Object> authMethodsOTP;
  private Map<String, Object> authMethodsSAML;
  private Map<String, Object> authMethodsOAuth;
  private Map<String, Object> authMethodsWebAuthn;
  private Map<String, Object> authMethodsTOTP;
  private Map<String, Object> messagingProvidersWeb;
  private Map<String, Object> authMethodsEnchantedLink;
  private Map<String, Object> authMethodsPassword;
  private Map<String, Object> authMethodsOIDCIDP;
  private Map<String, Object> authMethodsEmbeddedLink;
  private String tag;
}
