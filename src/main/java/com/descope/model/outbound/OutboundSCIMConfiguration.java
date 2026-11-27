package com.descope.model.outbound;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundSCIMConfiguration {
  private String appId;
  private Map<String, Object> configuration;
  private Boolean enabled;
  private Integer lastExportTime;
  private Integer lastProcessingTime;
  private Integer failures;
  private Long version;
}
