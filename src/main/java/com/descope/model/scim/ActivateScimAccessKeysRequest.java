package com.descope.model.scim;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivateScimAccessKeysRequest {
  /** Required. */
  private String tenantId;
  /** The SSO configuration the keys belong to. Omit for the tenant's default configuration. */
  private String ssoId;
  /** Required. The keys to activate, up to 100, from {@code loadKeys}. */
  private List<String> keyIds;
}
