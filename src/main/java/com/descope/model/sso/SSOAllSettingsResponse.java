package com.descope.model.sso;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOAllSettingsResponse {
  @JsonProperty("SSOSettings")
  private List<SSOTenantSettingsResponse> ssoSettings;
}
