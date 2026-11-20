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
  private boolean addToLoginIds;
  private boolean onMergeUseExisting;
  private String providerId;
  private NOTPTemplateIds templates;
  private Map<String, String> templateOptions;
  private String ssoAppId;
  private String locale;
  private boolean failOnConflict;
}
