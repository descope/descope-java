package com.descope.model.user.request;

import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSignUpEmbeddedLinkRequest {
  private String loginId;
  private User user;
  private Boolean emailVerified;
  private Boolean phoneVerified;
  private LoginOptions loginOptions;
  private int timeout;
}
