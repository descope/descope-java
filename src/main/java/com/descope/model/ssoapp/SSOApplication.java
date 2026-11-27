package com.descope.model.ssoapp;

import java.util.List;
import java.util.Map;
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
  private String templateId;
  private SSOApplicationCustomSettings customSettings;
  private SSOApplicationWSFedSettings wsfedSettings;
  /** Sibling SSO apps whose roles are included in this app's JWT under the {@code trustedApps} claim. */
  private List<String> trustedAppIds;
  /** The app's custom attribute values, keyed by attribute name. */
  private Map<String, Object> customAttributes;
  /** Application roles this app has in its trusted apps, when client credentials are enabled. */
  private List<SSOAppServiceAccountRole> serviceAccountRoles;
  /** Epoch seconds. */
  private Long createdTime;
  private String darkLogo;
  /** Project-level roles that grant access to this application. */
  private List<String> assignedRoleIds;

  public SSOApplication(String id, String name, String description, Boolean enabled, String logo, String appType,
      SSOApplicationSAMLSettings samlSettings, SSOApplicationOIDCSettings oidcSettings) {
    this(id, name, description, enabled, logo, appType, samlSettings, oidcSettings, null, null, null, null, null, null,
        null, null, null);
  }
}
