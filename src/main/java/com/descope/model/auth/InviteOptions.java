package com.descope.model.auth;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteOptions {
  private String inviteUrl;
  private String templateId;
  private Map<String, String> templateOptions;
  private Boolean sendEmail;
  private Boolean sendSMS;
}
