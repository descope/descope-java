package com.descope.model.ssoapp;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOAppServiceAccountRole {
  /** The app the roles are defined on. Required. */
  private String targetAppId;
  private List<String> roleIds;
}
