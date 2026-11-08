package com.descope.model.otp;

import com.descope.model.magiclink.LoginOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {

  private String loginId;
  private LoginOptions loginOptions;
  private String providerId;
  private String ssoAppId;
  private Boolean useRecovery;

  public SignInRequest(String loginId, LoginOptions loginOptions) {
    this.loginId = loginId;
    this.loginOptions = loginOptions;
  }
}
