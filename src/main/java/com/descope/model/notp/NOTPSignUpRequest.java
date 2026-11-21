package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignUpRequest {
  private String provider;
  private String phone;
  private String loginId;
  private NOTPSignUpUser user;
  private String providerId;
  private NOTPTemplateIds templates;
  private String ssoAppId;
  private NOTPSignUpOptions loginOptions;
}
