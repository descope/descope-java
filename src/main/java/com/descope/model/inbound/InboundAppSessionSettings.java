package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Session settings of an inbound application. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppSessionSettings {
  public static final String UNIT_SECONDS = "seconds";
  public static final String UNIT_MINUTES = "minutes";
  public static final String UNIT_HOURS = "hours";
  public static final String UNIT_DAYS = "days";
  public static final String UNIT_WEEKS = "weeks";

  private Boolean enabled;
  private Integer refreshTokenExpiration;
  /** Required. One of the {@code UNIT_*} values. */
  private String refreshTokenExpirationUnit;
  private Integer sessionTokenExpiration;
  /** Required. One of the {@code UNIT_*} values. */
  private String sessionTokenExpirationUnit;
  private String userTemplateId;
  private String keyTemplateId;
  private Integer keySessionTokenExpiration;
  /** Required. One of the {@code UNIT_*} values. */
  private String keySessionTokenExpirationUnit;
  /** Revoke a client's refresh token after inactivity. Custom mode only ({@code enabled} true). */
  private Boolean enableInactivity;
  private Integer inactivityTime;
  /** One of the {@code UNIT_*} values. */
  private String inactivityTimeUnit;
}
