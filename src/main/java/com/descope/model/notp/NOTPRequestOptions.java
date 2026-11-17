package com.descope.model.notp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPRequestOptions {
  private String providerId;
  private NOTPTemplates templates;
  private String ssoAppId;
}
