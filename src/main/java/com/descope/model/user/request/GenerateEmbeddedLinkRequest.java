package com.descope.model.user.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateEmbeddedLinkRequest {
  private String loginId;
  private Map<String, Object> customClaims;
}
