package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplication {
  private String id;
  private String name;
  private String description;
  private Boolean enabled;
  private String logo;
  private String appType;
  private SSOApplicationSAMLSettings samlSettings;
  private SSOApplicationOIDCSettings oidcSettings;
}
