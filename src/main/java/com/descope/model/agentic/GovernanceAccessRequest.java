package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// A request to provision an agent client, reviewed by an Agentic Governance Suite admin.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequest {
  private String id;
  private String requestorUserId;
  private String requestorName;
  private String clientName;
  private String clientType;
  private List<String> grantTypes;
  private List<GovernanceAccessRequestTarget> targets;
  private List<String> scopes;
  private String justification;
  // "pending", "approved" or "denied"
  private String status;
  private String resolvedByUserId;
  private String resolvedByName;
  // The deny reason or the approve comment, depending on status
  private String resolutionNote;
  // The client id of the agent client provisioned on approval
  private String clientId;
  // The application id the agent client is provisioned with
  private String clientAppId;
  private long createdTime;
  private long modifiedTime;
  // The provisioned client has since been deleted
  private boolean clientDeleted;
}
