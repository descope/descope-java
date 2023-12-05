package com.descope.model.enchantedlink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkResponse {
  private String pendingRef;
  private String linkId;
  private String maskedEmail;
}
