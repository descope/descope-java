package com.descope.model.enchantedlink;

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
  // Client-side LoginOptions properties the API doesn't accept
  @JsonIgnoreProperties({"jwt", "jwtrequired"})
  private LoginOptions loginOptions;
  private String providerId;
  private String templateId;
  private String ssoAppId;
  private Boolean useRecovery;
}
