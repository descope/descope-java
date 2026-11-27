package com.descope.model.flow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagementConfigTriggers {
  public static final String TRIGGER_TYPE_SCHEDULE = "schedule";
  public static final String TRIGGER_TYPE_EVENT = "event";

  private Boolean enabled;
  /** One of the TRIGGER_TYPE_ constants. */
  private String triggerType;
  private ManagementConfigEventTriggers eventTriggers;
}
