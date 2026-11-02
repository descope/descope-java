package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequest {
  private String id;
  private String requestorUserId;
  private String requestorName;
  private String clientType;
  private List<String> grantTypes;
  private List<GovernanceAccessRequestTarget> targets;
  private List<String> scopes;
  private String justification;
  // "pending", "approved" or "denied"
  private String status;
  private String resolvedByUserId;
  private String resolvedByName;
  private String resolutionNote;
  private String clientId;
  private String clientAppId;
  private long createdTime;
  private long modifiedTime;
  private long version;
  private String clientName;
  private boolean clientDeleted;
}
