package com.descope.model.outbound;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadOutboundAppTenantTokenRequest {
  private String appId;
  private String tenantId;
  private String refreshToken;
  private String accessToken;
  // Epoch seconds
  private Integer accessTokenExpiry;
  private String accessTokenType;
  private List<String> scopes;
  private String externalIdentifier;
  private String idToken;
  private String grantedBy;
  // Epoch seconds
  private Integer refreshTokenExpiry;
  // Verify the refresh token with the provider before saving it
  private Boolean verifyRefresh;
}
