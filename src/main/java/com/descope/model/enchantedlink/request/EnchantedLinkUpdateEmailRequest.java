package com.descope.model.enchantedlink.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnchantedLinkUpdateEmailRequest {
  private String loginId;
  private String email;
  private String redirectUrl;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private String providerId;
  private Map<String, String> templateOptions;
  private String locale;
  private Boolean failOnConflict;
}
