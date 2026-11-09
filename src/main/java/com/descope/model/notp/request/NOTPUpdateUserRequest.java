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
public class NOTPUpdateUserRequest {
  private String provider;
  private String loginId;
  private String phone;
  @JsonProperty("addToLoginIDs")
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private String providerId;
  private NOTPTemplates templates;
  private Map<String, String> templateOptions;
  private String ssoAppId;
  private String locale;
  private Boolean failOnConflict;
}
