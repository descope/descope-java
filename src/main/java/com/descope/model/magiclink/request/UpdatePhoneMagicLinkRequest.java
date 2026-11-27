package com.descope.model.magiclink.request;

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
public class UpdatePhoneMagicLinkRequest {
  private String phone;
  private String loginId;
  private String redirectUrl;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private Map<String, String> templateOptions;
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;
}
