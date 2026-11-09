package com.descope.model.enchantedlink.request;

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
public class EnchantedLinkSignInRequest {
  private String loginId;
  private String redirectUrl;
  // Derived getter, not part of the API
  @JsonIgnoreProperties("jwtrequired")
  private LoginOptions loginOptions;
  private String providerId;
  private String ssoAppId;
  private Boolean useRecovery;
}
