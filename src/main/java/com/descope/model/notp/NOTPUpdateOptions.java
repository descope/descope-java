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
  private Map<String, String> templateOptions;
  private String providerId;
  private NOTPTemplates templates;
  private String ssoAppId;
  /** Locale for the message, max 254 characters. */
  private String locale;
  private Boolean failOnConflict;
}
