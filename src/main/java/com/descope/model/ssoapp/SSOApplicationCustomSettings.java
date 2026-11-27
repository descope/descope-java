package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplicationCustomSettings {
  private String loginPageUrl;
  private SSOApplicationSessionSettings sessionSettings;
}
