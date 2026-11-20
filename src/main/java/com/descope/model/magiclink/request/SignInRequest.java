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
  private String ssoAppId;
  private Boolean useRecovery;

  // Keeps the pre-providerId all-args constructor available to callers that use it positionally.
  public SignInRequest(String uri, String loginId, LoginOptions loginOptions) {
    this(uri, loginId, loginOptions, null, null, null);
  }
}
