package com.descope.model.scim;

import com.descope.model.mgmt.AccessKeyResponseDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScimAccessKey {
  private AccessKeyResponseDetails key;
  /** The SSO configuration this key provisions into. */
  private String ssoId;
}
