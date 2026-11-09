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
public class PasswordResetSendRequestBody {
  private String loginId;
  private String redirectUrl;
  private String providerId;
  private Map<String, String> templateOptions;
  private String locale;
  private Boolean useRecovery;
}
