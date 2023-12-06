package com.descope.model.user.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderTokenResponse {
  String provider;
  String providerUserId;
  String accessToken;
  Long expiration;
  List<String> scopes;
}
