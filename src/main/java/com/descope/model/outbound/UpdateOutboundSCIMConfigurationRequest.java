package com.descope.model.outbound;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOutboundSCIMConfigurationRequest {
  private String appId;
  private Map<String, Object> configuration;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long version;
}
