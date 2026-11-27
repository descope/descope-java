package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A developer's request to provision an agent client, reviewed by an Agentic Governance Suite admin. */
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
  /** "pending", "approved" or "denied". */
  private String status;
  private String resolvedByUserId;
  private String resolvedByName;
  private String resolutionNote;
  /** The client id of the agent client provisioned on approval. */
  private String clientId;
  private String clientAppId;
  private Long createdTime;
  private Long modifiedTime;
  private String clientName;
  /** The provisioned client has since been deleted. */
  private Boolean clientDeleted;
}
