package com.descope.model.resource;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRegistrationTemplateRequest {
  private String name;
  private String description;
  private String loginPageURL;
  private ThirdPartyApplicationSessionSettings sessionSettings;
  private Boolean skipConsentScreen;
  private Boolean forceAddAllAuthorizationInfo;
  private List<String> tags;
  private String fallbackClientLogo;
}
