package com.descope.model.notp.request;

import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.notp.NOTPTemplates;
import com.descope.model.user.User;
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
  private User user;
  private String providerId;
  private NOTPTemplates templates;
  private String ssoAppId;
  private SignUpOptions loginOptions;
}
