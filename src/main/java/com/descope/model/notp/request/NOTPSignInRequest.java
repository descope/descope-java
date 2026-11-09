package com.descope.model.notp.request;

import com.descope.model.notp.NOTPTemplates;
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
  private NOTPTemplates templates;
  private String ssoAppId;
}
