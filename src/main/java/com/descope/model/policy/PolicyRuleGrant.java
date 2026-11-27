package com.descope.model.policy;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRuleGrant {
  private List<String> scopes;
  private List<String> allowedAudiences;
  private Boolean allScopes;
}
