package com.descope.model.enchantedlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional parameters for enchanted link sign-up requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkSignUpOptions {
  private String providerId;
  private String templateId;
}
