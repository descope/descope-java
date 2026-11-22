package com.descope.model.password;

import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("redirectUrl")
  private String redirectURL;
  private Map<String, String> templateOptions;
}
