package com.descope.model.password;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationPasswordResetRequestBody {
  private String loginId;
  private String redirectURL;
  private Map<String, String> templateOptions;
}
