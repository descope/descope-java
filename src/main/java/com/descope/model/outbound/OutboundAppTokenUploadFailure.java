package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundAppTokenUploadFailure {
  private String appId;
  private String userId;
  private String tenantId;
  private String errorCode;
  private String reason;
}
