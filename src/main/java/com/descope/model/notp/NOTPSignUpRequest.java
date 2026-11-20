package com.descope.model.notp;

import com.descope.enums.NOTPProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignUpRequest {
  private NOTPProvider provider;
  private String phone;
  private String loginId;
  private NOTPSignUpUser user;
  private String providerId;
  private NOTPTemplateIds templates;
  private String ssoAppId;
  private NOTPSignUpOptions loginOptions;
}
