package com.descope.model.enchantedlink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnchantedLinkResponse {
  private String pendingRef;
  private String linkId;
  private String maskedEmail;
}
