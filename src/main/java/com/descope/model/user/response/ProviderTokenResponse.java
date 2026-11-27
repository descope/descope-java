package com.descope.model.user.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderTokenResponse {
  String provider;
  String providerUserId;
  String accessToken;
  Long expiration;
  List<String> scopes;
  @ToString.Exclude
  String refreshToken;

  public ProviderTokenResponse(String provider, String providerUserId, String accessToken, Long expiration,
      List<String> scopes) {
    this(provider, providerUserId, accessToken, expiration, scopes, null);
  }
}
