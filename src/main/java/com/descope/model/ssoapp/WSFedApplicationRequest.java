package com.descope.model.ssoapp;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WSFedApplicationRequest {
  /**
   * Optional ID that if given must be unique per project. Will be generated if not given.
   */
  private String id;
  /**
   * The sso application's name. Must be unique per project.
   */
  private String name;
  private String description;
  private Boolean enabled;
  private String logo;
  /**
   * The URL where login page is hosted.
   */
  private String loginPageUrl;
  private String realm;
  private String replyUrl;
  private List<SAMLIDPAttributeMappingInfo> attributeMapping;
  private List<SAMLIDPGroupsMappingInfo> groupsMapping;
  private Boolean forceAuthentication;
  private String logoutRedirectUrl;
  private String errorRedirectUrl;
  private List<String> replyAllowedCallbacks;
  /**
   * The app's custom attribute values, keyed by attribute name.
   */
  private Map<String, Object> customAttributes;
  private String darkLogo;
  /**
   * Project-level roles that grant access to this application, in addition to users assigned directly or through
   * their tenant.
   */
  private List<String> assignedRoleIds;
}
