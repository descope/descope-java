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
public class NOTPSignInRequest {
  private NOTPProvider provider;
  private String loginId;
  private NOTPLoginOptions loginOptions;
  private String providerId;
  private NOTPTemplateIds templates;
  private String ssoAppId;
}
