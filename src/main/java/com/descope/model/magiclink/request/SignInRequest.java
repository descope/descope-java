package com.descope.model.magiclink.request;

import com.descope.model.magiclink.LoginOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {

  @JsonProperty("URI")
  private String uri;

  private String loginId;
  private LoginOptions loginOptions;
  private String providerId;
  private String templateId;
  private String ssoAppId;
  private Boolean useRecovery;

  // Keeps the previous all-args constructor for positional callers
  public SignInRequest(String uri, String loginId, LoginOptions loginOptions) {
    this.uri = uri;
    this.loginId = loginId;
    this.loginOptions = loginOptions;
  }
}
