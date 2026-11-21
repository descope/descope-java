package com.descope.model.enchantedlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional parameters for enchanted link sign-in and sign-up-or-in requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkSignInOptions {
  private String providerId;
  private String templateId;
  private String ssoAppId;
  private Boolean useRecovery;
}
