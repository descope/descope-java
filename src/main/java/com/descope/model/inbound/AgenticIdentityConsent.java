package com.descope.model.inbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentityConsent {
  private String id;
  private String appId;
  private String userId;
  private String[] scopes;
  private String grantedBy;
  private int createdTime;
  private String tenantId;
  @JsonProperty("ExpireTime")
  private int expireTime;
  private int modifiedTime;
}
