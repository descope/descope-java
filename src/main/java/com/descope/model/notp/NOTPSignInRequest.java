package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignInRequest {
  private String provider;
  private String loginId;
  private NOTPLoginOptions loginOptions;
  private String providerId;
  private NOTPTemplateIds templates;
  private String ssoAppId;
}
