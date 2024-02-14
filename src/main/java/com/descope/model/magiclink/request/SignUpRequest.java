package com.descope.model.magiclink.request;

import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
  private String email;
  private String loginId;
  private User user;

  @JsonProperty("URI")
  private String uri;
  private SignUpOptions loginOptions;
}
