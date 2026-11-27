package com.descope.model.policy;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule {
  /** Server-generated on create; identifies the rule on update. */
  private String id;
  /** Sent as a string on the wire. */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long version;
  /** Required. */
  private String name;
  private String description;
  private Boolean enabled;
  private String ruleFamily;
  private List<String> actionKinds;
  private String effect;
  private String principalType;
  private List<String> principalSelector;
  private List<PolicyRuleResourceTarget> resourceTargets;
  private List<PolicyRuleGrant> grants;
  /** Structured conditions, AND-ed. At most 255. */
  private List<PolicyRuleCondition> conditions;
  /** Epoch seconds. */
  private Integer createdTime;
  /** Epoch seconds. */
  private Integer modifiedTime;
  /** Server-assigned 1-based evaluation position; changes only via reorder. */
  private Integer evaluationOrder;
  /** Epoch seconds after which the rule is no longer evaluated; 0 means never. */
  private Integer expireTime;
}
