package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppConsentCreateRequest {
  /** Required. */
  private String appId;
  /** Required. */
  private String userId;
  /** Scopes defined by the app, including its mandatory ones. Up to 1000. */
  private String[] scopes;
  /** The user must be a member of this tenant. */
  private String tenantId;
  /** Epoch seconds; 0 or unset means the consent never expires. */
  private Integer expireTime;
}
