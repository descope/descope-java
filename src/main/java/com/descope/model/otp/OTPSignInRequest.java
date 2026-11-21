package com.descope.model.otp;

import com.descope.model.magiclink.LoginOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPSignInRequest {
  private String loginId;
  // SDK-side LoginOptions members that the API doesn't define
  @JsonIgnoreProperties({"jwtrequired", "jwt"})
  private LoginOptions loginOptions;
  private String providerId;
  private String templateId;
  private String ssoAppId;
  private Boolean useRecovery;
}
