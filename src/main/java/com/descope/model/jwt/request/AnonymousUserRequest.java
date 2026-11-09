package com.descope.model.jwt.request;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AnonymousUserRequest {
  private Map<String, Object> customClaims;
  private String selectedTenant;
  private int refreshDuration;
  private List<String> roles;

  public AnonymousUserRequest(Map<String, Object> customClaims, String selectedTenant, int refreshDuration) {
    this(customClaims, selectedTenant, refreshDuration, null);
  }
}