package com.descope.model.ssoapp;

import com.descope.model.sso.JWTBearerSettings;
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
public class OIDCApplicationRequest {
  public static final String CLIENT_TYPE_CONFIDENTIAL = "confidential";
  public static final String CLIENT_TYPE_PUBLIC = "public";
  public static final String AUDIENCE_PROJECT_ID = "projectId";
  public static final String AUDIENCE_CLIENT_ID = "clientId";
  public static final String AUDIENCE_APP_ID = "appId";
  public static final String AUDIENCE_EMPTY = "empty";
  public static final String LOGOUT_PROPAGATION_NONE = "none";
  public static final String LOGOUT_PROPAGATION_ALL_APPS = "allApps";
  public static final String NO_SCOPE_BEHAVIOR_LEGACY = "legacyNoScopePolicy";
  public static final String NO_SCOPE_BEHAVIOR_GRANT_DEFAULT_SCOPES = "grantDefaultScopes";
  public static final String NO_SCOPE_BEHAVIOR_GRANT_ALL_SCOPES = "grantAllScopes";

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
  /**
   * Optional claims of the app.
   */
  private List<String> claims;
  /**
   * Optional back-channel logout URL. Must use https.
   */
  private String backChannelLogoutUrl;
  /**
   * Optional JWT bearer settings. On update, null keeps the current settings and an empty object clears them.
   */
  private JWTBearerSettings jwtBearerSettings;
  private String customIdpInitiatedLoginPageUrl;
  /**
   * Optional token lifetimes and JWT templates of the app. On update, null keeps the current settings, and
   * {@code enabled} false uses the project's.
   */
  private SSOApplicationSessionSettings sessionSettings;
  /**
   * Optional OAuth client type, one of the {@code CLIENT_TYPE_*} values. Empty on create means the legacy type.
   */
  private String clientType;
  private List<String> approvedRedirectUrls;
  private Boolean authorizationCodeDisabled;
  private Boolean clientCredentialsDisabled;
  private Boolean refreshTokenDisabled;
  private Boolean jwtBearerDisabled;
  private Boolean deviceCodeDisabled;
  private Boolean forcePkce;
  /**
   * Optional client ID to import an existing OIDC client, unique in the project. Accepted on create only; empty means
   * the server computes it.
   */
  private String clientId;
  /**
   * Optional client secret to import an existing OIDC client. Accepted on create only; empty means the server
   * generates it.
   */
  private String clientSecret;
  /**
   * Optional default audience policy, one of the {@code AUDIENCE_*} values, or empty for both project and client ID.
   */
  private String defaultAudience;
  /**
   * Optional sibling SSO apps whose roles are included in this app's JWT under the {@code trustedApps} claim.
   */
  private List<String> trustedAppIds;
  /**
   * Optional custom attribute values of the app, keyed by attribute name. On update, null keeps the current values
   * and an empty map clears them.
   */
  private Map<String, Object> customAttributes;
  /**
   * Optional audience policy for trusted apps' identifiers, one of the {@code AUDIENCE_*} values.
   */
  private String trustedAppsAudience;
  /**
   * Optional application roles this app gets in its trusted apps, when client credentials are enabled.
   */
  private List<SSOAppServiceAccountRole> serviceAccountRoles;
  private MTLSClientAuthenticationMethods mtlsClientAuthMethods;
  /**
   * Optional sso application logo for dark mode.
   */
  private String darkLogo;
  private WellKnownCustomization wellKnownCustomization;
  /**
   * Optional project-level roles that grant access to this application. Tenant scoped roles are rejected.
   */
  private List<String> assignedRoleIds;
  /**
   * Optional: require a valid DPoP proof at the token endpoint.
   */
  private Boolean forceDpop;
  /**
   * Optional reach of a logout at the app's end-session endpoint, one of the {@code LOGOUT_PROPAGATION_*} values. An
   * update without it sets it back to none.
   */
  private String logoutPropagation;
  /**
   * Optional: a Force Logout of the user sends the app a back-channel logout token. Needs backChannelLogoutUrl.
   */
  private Boolean notifyOnForceLogout;
  /**
   * Optional grant when a request names no scopes, one of the {@code NO_SCOPE_BEHAVIOR_*} values. Null keeps the
   * stored behavior.
   */
  private String noScopeBehavior;
  /**
   * Optional scopes granted under {@link #NO_SCOPE_BEHAVIOR_GRANT_DEFAULT_SCOPES}. Up to 100 scopes.
   */
  private List<String> defaultScopes;

  public OIDCApplicationRequest(String id, String name, String description, Boolean enabled, String logo,
      String loginPageUrl, Boolean forceAuthentication) {
    this(id, name, description, enabled, logo, loginPageUrl, forceAuthentication, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null);
  }
}
