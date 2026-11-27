package com.descope.model.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyApplicationSessionSettings {
  /** Time unit values for the expiration and inactivity unit fields. */
  public static final String TIME_UNIT_SECONDS = "seconds";
  public static final String TIME_UNIT_MINUTES = "minutes";
  public static final String TIME_UNIT_HOURS = "hours";
  public static final String TIME_UNIT_DAYS = "days";
  public static final String TIME_UNIT_WEEKS = "weeks";

  private Boolean enabled;
  private Integer refreshTokenExpiration;
  private String refreshTokenExpirationUnit;
  private Integer sessionTokenExpiration;
  private String sessionTokenExpirationUnit;
  private String userTemplateId;
  private String keyTemplateId;
  private Integer keySessionTokenExpiration;
  private String keySessionTokenExpirationUnit;
  /** Custom mode only (enabled=true): reject a refresh after inactivityTime without use. */
  private Boolean enableInactivity;
  private Integer inactivityTime;
  private String inactivityTimeUnit;
}
