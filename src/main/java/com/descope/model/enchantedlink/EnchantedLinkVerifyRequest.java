package com.descope.model.enchantedlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkVerifyRequest {
  private String token;
  private String pkceVerifier;
}
