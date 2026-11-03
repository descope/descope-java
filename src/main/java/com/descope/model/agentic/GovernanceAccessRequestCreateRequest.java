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
public class GovernanceAccessRequestCreateRequest {
  // The console user the request is raised for
  private String requestorUserId; // required
  private String clientName;
  private String clientType;
  // authCode, clientCredentials, jwtBearer or ciba
  private List<String> grantTypes;
  private List<GovernanceAccessRequestTarget> targets;
  private List<String> scopes;
  private String justification;
}
