package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplicationSessionSettings {
  public static final String UNIT_SECONDS = "seconds";
  public static final String UNIT_MINUTES = "minutes";
  public static final String UNIT_HOURS = "hours";
  public static final String UNIT_DAYS = "days";
  public static final String UNIT_WEEKS = "weeks";

  /** Use the app's own session settings. When false, the project's settings apply. */
  private Boolean enabled;
  private Integer refreshTokenExpiration;
  /** One of the {@code UNIT_*} values. Required. */
  private String refreshTokenExpirationUnit;
  private Integer sessionTokenExpiration;
  /** One of the {@code UNIT_*} values. Required. */
  private String sessionTokenExpirationUnit;
  private String userTemplateId;
  private String keyTemplateId;
  private Integer keySessionTokenExpiration;
  /** One of the {@code UNIT_*} values. Required. */
  private String keySessionTokenExpirationUnit;
  /** Inbound apps and MCP servers only; ignored on SSO applications. */
  private Boolean enableInactivity;
  /** Inbound apps and MCP servers only; ignored on SSO applications. */
  private Integer inactivityTime;
  /** Inbound apps and MCP servers only; ignored on SSO applications. One of the {@code UNIT_*} values. */
  private String inactivityTimeUnit;
}
