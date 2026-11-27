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
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean useRecovery;

  public AuthenticationPasswordResetRequestBody(String loginId, String redirectURL,
      Map<String, String> templateOptions) {
    this(loginId, redirectURL, templateOptions, null, null, null, null);
  }
}
