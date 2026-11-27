package com.descope.model.sso;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XAASettingsResponse {
  private String ssoId;
  private Boolean enabled;
  private JWTBearerSettings settings;
  /** Group to role mappings, with roles given by ID and name. */
  private List<GroupsMapping> groupsMapping;
  private List<String> defaultSSORoles;
  private Map<String, ReBACGroupsMapping> fgaMappings;
  private List<String> groupsPriority;
  private Boolean groupPriorityEnabled;
  private Boolean allowOverrideRoles;
  private String providerID;
  /** Read-only: the project-level audience a requesting application must present in its ID-JAG token. */
  private String audience;
  private List<ReBACGroupsMappingRelation> defaultFgaRelations;
}
