package com.descope.model.inbound;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppConsent {
  private String id;
  private String appId;
  private String userId;
  private String []scopes;
  private String grantedBy;
  private long createdTime;
  private String tenantId;
  /** Epoch seconds; 0 means the consent never expires. */
  @JsonProperty("ExpireTime")
  @JsonAlias("expireTime")
  private Long expireTime;
  private Long modifiedTime;
  /** Empty for an explicit consent, "idJag" for an identity recorded by a Cross-App Access exchange. */
  private String authMethod;
  /** True when an admin blocked this "idJag" identity. */
  private Boolean blocked;

  public InboundAppConsent(String id, String appId, String userId, String[] scopes, String grantedBy,
      long createdTime, String tenantId) {
    this.id = id;
    this.appId = appId;
    this.userId = userId;
    this.scopes = scopes;
    this.grantedBy = grantedBy;
    this.createdTime = createdTime;
    this.tenantId = tenantId;
  }
}

