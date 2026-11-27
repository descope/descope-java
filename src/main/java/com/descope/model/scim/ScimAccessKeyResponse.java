package com.descope.model.scim;

import com.descope.model.mgmt.AccessKeyResponseDetails;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScimAccessKeyResponse {
  /** The bearer token to configure in the identity provider. Returned exactly once. */
  private String cleartext;
  private AccessKeyResponseDetails key;
  /** The SCIM base URL to configure in the identity provider. */
  private String scimUrl;
  /** The keys revoked by this call, if any. */
  private List<String> revokedKeyIds;
}
