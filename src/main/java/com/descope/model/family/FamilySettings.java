package com.descope.model.family;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Project family settings. When configuring, fields left null keep their current value. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FamilySettings {
  private Boolean enabled;
  /** The maximum number of members in a family, at least 1. */
  private Integer maxFamilyMembers;
  private Boolean allowMultipleFamiliesUsers;
}
