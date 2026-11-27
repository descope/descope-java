package com.descope.model.agentic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A consent granted to an agentic app. */
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
  private Long createdTime;
  private String tenantId;
  @JsonProperty("ExpireTime")
  private Long expireTime;
  private Long modifiedTime;
  /** Empty for an explicit consent, "idJag" for an identity recorded by a Cross-App Access exchange. */
  private String authMethod;
  /** True when an admin blocked this "idJag" identity. */
  private Boolean blocked;
}
