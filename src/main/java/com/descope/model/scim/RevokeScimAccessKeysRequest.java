package com.descope.model.scim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevokeScimAccessKeysRequest {
  /** Required. */
  private String tenantId;
  /** The SSO configuration to revoke. Omit for the tenant's default configuration, not every configuration. */
  private String ssoId;
  /** One of {@link ScimKeyRevokeMode}. Omit to delete the keys. */
  private String revokeMode;
}
