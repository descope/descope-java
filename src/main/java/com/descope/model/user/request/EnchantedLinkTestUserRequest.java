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
@SuppressWarnings("checkstyle:MemberName")
public class EnchantedLinkTestUserRequest {
  private String loginId;
  private String URI;
  private String redirectUrl;
  // Not part of the test-user login options
  @JsonIgnoreProperties({"jwtrequired", "jwt"})
  private LoginOptions loginOptions;

  public EnchantedLinkTestUserRequest(String loginId, String uri) {
    this.loginId = loginId;
    this.URI = uri;
  }
}
