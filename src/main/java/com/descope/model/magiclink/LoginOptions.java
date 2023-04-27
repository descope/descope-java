package com.descope.model.magiclink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginOptions {
  private boolean stepUp;
  private boolean mfa;
  private Map<String, Object> customClaims;
}
