package com.descope.model.otp;

import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
  private String whatsApp;
  private String phone;
  private String email;
  private String loginId;
  private User user;
  private SignUpOptions loginOptions;
  private String providerId;
  private String templateId;

  public SignUpRequest(String whatsApp, String phone, String email, String loginId, User user,
      SignUpOptions loginOptions) {
    this(whatsApp, phone, email, loginId, user, loginOptions, null, null);
  }
}
