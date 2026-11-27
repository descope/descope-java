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
public class CreateOutboundSCIMConfigurationRequest {
  private String appId;
  private Map<String, Object> configuration;
}
