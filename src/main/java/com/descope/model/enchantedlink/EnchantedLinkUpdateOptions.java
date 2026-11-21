package com.descope.model.enchantedlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional parameters for enchanted link email and phone update requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkUpdateOptions {
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;
}
