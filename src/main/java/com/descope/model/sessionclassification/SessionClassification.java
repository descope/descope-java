package com.descope.model.sessionclassification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A named set of session rules applied to the users assigned to it. The name is the id: unique per
 * project and immutable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionClassification {
  private String name;
  private String description;
  /** One of the {@link SessionTimeLimitRule} values. */
  private String sessionTimeLimitRule;
  /** Members may access every tenant in the project regardless of their explicit assignments. */
  private Boolean accessAllTenants;
  /** Overrides the project-wide limit for members. 0 means unlimited. */
  private Integer maxConcurrentSessions;
  /** Computed server side, ignored on write. */
  private Integer createdTime;
  /** Computed server side, ignored on write. */
  private Integer modifiedTime;
  /** Send back on an update to reject the write if someone else changed the classification. */
  private Integer version;
}
