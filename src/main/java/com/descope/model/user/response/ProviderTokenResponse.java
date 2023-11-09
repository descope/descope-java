package com.descope.model.user.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderTokenResponse {
  String provider;
  String providerUserId;
  String accessToken;
  Long expiration;
  List<String> scopes;
}
