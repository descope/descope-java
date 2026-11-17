package com.descope.model.notp.request;

import com.descope.model.notp.NOTPTemplates;
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
public class NOTPUpdateRequest {
  private String loginId;
  private String phone;
  @JsonProperty("addToLoginIDs")
  private Boolean addToLoginIds;
  private Boolean onMergeUseExisting;
  private Boolean failOnConflict;
  private String providerId;
  private NOTPTemplates templates;
  private Map<String, String> templateOptions;
  private String ssoAppId;
  private String locale;
}
