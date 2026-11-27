package com.descope.model.user.request;

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
public class OTPTestUserRequest {
  private String loginId;
  private String deliveryMethod;
  // Not part of the test-user login options
  @JsonIgnoreProperties({"jwtrequired", "jwt"})
  private LoginOptions loginOptions;

  public OTPTestUserRequest(String loginId, String deliveryMethod) {
    this.loginId = loginId;
    this.deliveryMethod = deliveryMethod;
  }
}
