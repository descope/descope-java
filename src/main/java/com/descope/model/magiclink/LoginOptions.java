package com.descope.model.magiclink;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginOptions {
  private boolean stepup;
  private boolean mfa;
  private Map<String, Object> customClaims;
}
