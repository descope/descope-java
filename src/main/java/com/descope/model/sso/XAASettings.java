package com.descope.model.sso;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cross-App Access (ID-JAG) settings of an SSO configuration. The role and group mapping is shared by the SAML, OIDC,
 * SCIM and Cross-App Access settings of the same SSO configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XAASettings {
  private Boolean enabled;
  /** Trusted issuers and grant configuration. */
  private JWTBearerSettings settings;
  /** Group to role mappings, with roles given by name. */
  private List<RoleMapping> roleMappings;
  private List<String> defaultSSORoles;
  /** FGA relations, keyed by group. */
  private Map<String, ReBACGroupsMapping> fgaMappings;
  private List<String> groupsPriority;
  private Boolean groupPriorityEnabled;
  private Boolean allowOverrideRoles;
  /** Selected IdP provider template ID, for display only. */
  private String providerID;
  /**
   * Verify identity only: logins through this SSO configuration do not create, update or sign in a user. {@code null}
   * keeps the stored value. It applies to the whole SSO configuration, not only its Cross-App Access settings.
   */
  private Boolean authenticationOnly;
  /**
   * FGA relations granted to every user who logs in through this SSO configuration when {@code fgaMappings} is empty.
   * Mutually exclusive with {@code fgaMappings}. An empty list keeps the stored relations unless
   * {@code replaceDefaultFgaRelations} is true.
   */
  private List<ReBACGroupsMappingRelation> defaultFgaRelations;
  /** When true, {@code defaultFgaRelations} replaces the stored relations, even when empty. */
  private Boolean replaceDefaultFgaRelations;
}
