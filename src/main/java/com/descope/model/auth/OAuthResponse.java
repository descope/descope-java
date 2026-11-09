package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthResponse {
  private String url;
  /** Set when the server redirects to an error page instead of the provider. */
  private String errorId;

  public OAuthResponse(String url) {
    this(url, null);
  }
}
