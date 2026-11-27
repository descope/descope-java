package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A request to provision an agent client on behalf of a console user. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestCreateRequest {
  /** Required: an existing, active console user in the company the request is raised for. */
  private String requestorUserId;
  private String clientName;
  private String clientType;
  /** The OAuth grant types requested, see {@link GovernanceGrantType}. */
  private List<String> grantTypes;
  private List<GovernanceAccessRequestTarget> targets;
  private List<String> scopes;
  private String justification;
}
