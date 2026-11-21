package com.descope.model.notp;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPSignUpOptions {
  private Map<String, Object> customClaims;
  private Map<String, String> templateOptions;
  private String locale;
  private String pkceChallenge;
  private String templateId;
  private String tenantId;
}
