package com.descope.model.enchantedlink;

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
public class EnchantedLinkSignUpRequest {
  private String email;
  private String phone;
  private String loginId;
  private User user;
  private String redirectUrl;
  private String providerId;
  private String templateId;
  private SignUpOptions loginOptions;
}
