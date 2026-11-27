package com.descope.model.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOSetupSuiteSettings {
  Boolean enabled;
  String styleId;
  SSOSetupSuiteSettingsDisabledFeatures disabledFeatures;
}
