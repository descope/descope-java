package com.descope.model.notp;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPUpdateOptions {
  private Boolean addToLoginIds;
  private Boolean onMergeUseExisting;
  private Boolean failOnConflict;
  private Map<String, String> templateOptions;
  private String locale;
  private String providerId;
  private NOTPTemplateIds templates;
  private String ssoAppId;
}
