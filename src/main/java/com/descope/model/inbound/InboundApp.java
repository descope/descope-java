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
public class InboundApp {
  private String id;
  private String name;
  private String description; 
  private String logo;
  private String loginPageUrl;
  private String clientId;
  private String[] approvedCallbackUrls;
  private InboundAppScope[] permissionsScopes;
  private InboundAppScope[] attributesScopes;
  private Boolean dynamic;
  private String status;
  private String logoUrl;
  private InboundAppJwtBearerSettings jwtBearerSettings;
  private InboundAppSessionSettings sessionSettings;
  private Boolean nonConfidentialClient;
  private String[] audienceWhitelist;
  private Boolean forceAddAllAuthorizationInfo;
  private InboundAppScope[] connectionsScopes;
  /** One of the {@link InboundAppRequest} {@code DEFAULT_AUDIENCE_*} values; empty means both. */
  private String defaultAudience;
  private Boolean skipConsentScreen;
  private Boolean useResources;
  private Map<String, Object> customAttributes;
  private InboundAppCibaSettings cibaSettings;
  private InboundAppJarSettings jarSettings;
  private String[] allowedTenants;
  /** One of the {@link InboundAppRequest} {@code CLIENT_TYPE_*} values; empty for a legacy app. */
  private String clientType;
  private Boolean forcePkce;
  private Boolean allowPartialScopes;
  private InboundAppScopeClaimMapping[] scopeClaimMapping;
  private InboundAppMtlsClientAuthMethods mtlsClientAuthMethods;
  /** Epoch seconds. */
  private Integer createdTime;
  private Boolean forceDpop;
  private InboundAppRarSettings rarSettings;
  private Map<String, InboundAppIdJagTargetClient> idJagTargetClients;
  /** One of the {@link InboundAppRequest} {@code CLIENT_AUTH_METHOD_*} values. */
  private String clientAuthMethod;
  private InboundAppClientAssertion[] clientAssertions;
  private InboundAppWorkloadClientAssertion[] workloadClientAssertions;
  private Boolean forcePar;
  private Boolean forceMtls;
  private Boolean authorizationCodeDisabled;
  private Boolean clientCredentialsDisabled;
  private Boolean refreshTokenDisabled;
  private Boolean jwtBearerDisabled;
  private Boolean deviceCodeDisabled;
  private Boolean tokenExchangeDisabled;
  private Boolean cibaEnabled;
  private String[] tags;
  /** One of the {@link InboundAppRequest} {@code NO_SCOPE_BEHAVIOR_*} values. */
  private String noScopeBehavior;
  private String[] defaultScopes;
  private Boolean agentic;
  /** The owner's display name: their name, or else email, login ID or user ID. */
  private String createdBy;

  public InboundApp(String id, String name, String description, String logo, String loginPageUrl, String clientId,
      String[] approvedCallbackUrls, InboundAppScope[] permissionsScopes, InboundAppScope[] attributesScopes) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.logo = logo;
    this.loginPageUrl = loginPageUrl;
    this.clientId = clientId;
    this.approvedCallbackUrls = approvedCallbackUrls;
    this.permissionsScopes = permissionsScopes;
    this.attributesScopes = attributesScopes;
  }
}
