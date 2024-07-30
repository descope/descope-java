package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OIDCApplicationRequest {
  /**
   * Optional ID that if given must be unique per project. Will be generated if not given.
   */
  private String id;
  /**
   * The sso application's name. Must be unique per project.
   */
  private String name;
  /**
   * Optional sso application description.
   */
  private String description;
  /**
   * Optional set the sso application as enabled or disabled.
   */
  private Boolean enabled;
  /**
   * Optional sso application logo.
   */
  private String logo;
  /**
   * The URL where login page is hosted.
   */
  private String loginPageUrl;
  /**
   * Optional determine if the IdP should force the user to re-authenticate.
   */
  private Boolean forceAuthentication;
}
