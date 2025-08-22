package com.descope.model.jwt.request;

import com.descope.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AnonymousUserRequest {
  private Map<String, Object> customClaims;
  private String selectedTenant;
  private int refreshDuration;
}