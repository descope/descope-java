package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutboundAppByTemplateRequest {
  private String templateId;
  private String id;
  private String clientId;
  private String clientSecret;
  private String tenantId;
  private OutboundAppTemplateOverrides overrides;
}
