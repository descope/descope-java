package com.descope.model.agentic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// The consent an agentic identity was granted.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentityConsent {
  private String id;
  private String appId;
  private String userId;
  private List<String> scopes;
  private String grantedBy;
  private int createdTime;
  private String tenantId;
  @JsonProperty("ExpireTime")
  private int expireTime;
  private int modifiedTime;
  // Empty for an explicit consent, "idJag" for an identity recorded by a Cross-App Access exchange,
  // which cannot be revoked.
  private String authMethod;
}
