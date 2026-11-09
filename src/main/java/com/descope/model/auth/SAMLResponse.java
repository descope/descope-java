package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SAMLResponse {
  private String url;
  private String errorId;

  public SAMLResponse(String url) {
    this(url, null);
  }
}
