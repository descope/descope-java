package com.descope.model.family;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The project's family account settings. When configuring, unset (null) fields are left unchanged. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilySettings {
  Boolean enabled;
  Integer maxFamilyMembers;
  Boolean allowMultipleFamiliesUsers;
}
