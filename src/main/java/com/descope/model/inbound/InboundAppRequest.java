package com.descope.model.inbound;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppRequest {
  public static final String DEFAULT_AUDIENCE_PROJECT_ID = "projectId";
  public static final String DEFAULT_AUDIENCE_CLIENT_ID = "clientId";
  public static final String CLIENT_TYPE_CONFIDENTIAL = "confidential";
  public static final String CLIENT_TYPE_PUBLIC = "public";
  public static final String CLIENT_AUTH_METHOD_CLIENT_SECRET = "clientSecret";
  public static final String CLIENT_AUTH_METHOD_PRIVATE_KEY_JWT = "privateKeyJwt";
  public static final String CLIENT_AUTH_METHOD_WORKLOAD_IDENTITY = "workloadIdentity";
  public static final String CLIENT_AUTH_METHOD_MTLS = "mtls";
  public static final String NO_SCOPE_BEHAVIOR_LEGACY = "legacyNoScopePolicy";
  public static final String NO_SCOPE_BEHAVIOR_GRANT_DEFAULT_SCOPES = "grantDefaultScopes";
  public static final String NO_SCOPE_BEHAVIOR_GRANT_ALL_SCOPES = "grantAllScopes";

  private String id;
  private String name;
  private String description;
  private String logo;
  private String loginPageUrl;
  private String[] approvedCallbackUrls;
  private InboundAppScope[] permissionsScopes;
  private InboundAppScope[] attributesScopes;
  /** Create only. */
  private String consentFlowId;
  private String logoUrl;
  private InboundAppJwtBearerSettings jwtBearerSettings;
  private InboundAppSessionSettings sessionSettings;
  /** Create only. */
  private Boolean nonConfidentialClient;
  private String[] audienceWhitelist;
  /** Create only. */
  private String clientId;
  /** Create only. */
  private String clientSecret;
  private Boolean forceAddAllAuthorizationInfo;
  private InboundAppScope[] connectionsScopes;
  /** One of the {@code DEFAULT_AUDIENCE_*} values; empty means both. */
  private String defaultAudience;
  private Map<String, Object> customAttributes;
  private InboundAppCibaSettings cibaSettings;
  private InboundAppJarSettings jarSettings;
  private String[] allowedTenants;
  /** One of the {@code CLIENT_TYPE_*} values. Empty on create applies the account default. */
  private String clientType;
  private Boolean forcePkce;
  private Boolean allowPartialScopes;
  /** Replaces the stored mapping when set. */
  private InboundAppScopeClaimMapping[] scopeClaimMapping;
  private InboundAppMtlsClientAuthMethods mtlsClientAuthMethods;
  /** Only a confidential or public client type may require DPoP. */
  private Boolean forceDpop;
  private InboundAppRarSettings rarSettings;
  /** On patch, an absent or empty map keeps the stored mapping. */
  private Map<String, InboundAppIdJagTargetClient> idJagTargetClients;
  /** One of the {@code CLIENT_AUTH_METHOD_*} values; client secret when empty. */
  private String clientAuthMethod;
  /** The private_key_jwt trust entries. */
  private InboundAppClientAssertion[] clientAssertions;
  /** The workload identity trust entries. */
  private InboundAppWorkloadClientAssertion[] workloadClientAssertions;
  private Boolean forcePar;
  private Boolean forceMtls;
  private Boolean authorizationCodeDisabled;
  private Boolean clientCredentialsDisabled;
  private Boolean refreshTokenDisabled;
  private Boolean jwtBearerDisabled;
  private Boolean deviceCodeDisabled;
  private Boolean tokenExchangeDisabled;
  /** Allows the CIBA grant; requires {@code cibaSettings}. */
  private Boolean cibaEnabled;
  /** Up to 100 tags of up to 100 bytes each. On patch, an empty list keeps the stored tags. */
  private String[] tags;
  /** One of the {@code NO_SCOPE_BEHAVIOR_*} values: what to grant when a request names no scopes. */
  private String noScopeBehavior;
  /** Granted when {@code noScopeBehavior} is grantDefaultScopes and a request names no scopes. Up to 100. */
  private String[] defaultScopes;
  /** Makes the app an Agentic Identity Hub client (true) or removes it from there (false). */
  private Boolean agentic;
  /** The owner, as a user ID or login ID; needs the users read role. An empty string clears it. */
  private String createdBy;

  public InboundAppRequest(String id, String name, String description, String logo, String loginPageUrl,
      String[] approvedCallbackUrls, InboundAppScope[] permissionsScopes, InboundAppScope[] attributesScopes) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.logo = logo;
    this.loginPageUrl = loginPageUrl;
    this.approvedCallbackUrls = approvedCallbackUrls;
    this.permissionsScopes = permissionsScopes;
    this.attributesScopes = attributesScopes;
  }
}
