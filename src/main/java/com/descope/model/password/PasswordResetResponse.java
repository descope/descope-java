package com.descope.model.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
  private String resetMethod;
  private String pendingRef;
  private String linkId;
  private String maskedEmail;
}
