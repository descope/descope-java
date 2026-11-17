package com.descope.model.notp.request;

import com.descope.model.notp.NOTPSignUpOptions;
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
  private String loginId;
  private String phone;
  private User user;
  private NOTPSignUpOptions loginOptions;
  private String providerId;
  private NOTPTemplates templates;
  private String ssoAppId;
}
