package com.descope.client;

import com.descope.exception.ClientSetupException;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import com.descope.model.client.ClientParams;
import com.descope.model.client.SdkInfo;
import com.descope.model.magement.ManagementParams;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.mgmt.ManagementService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import io.jsonwebtoken.lang.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Getter
// The main entry point for working with the Descope SDK.
public class DescopeClient {

  private static final String DEFAULT_BASE_URL = "https://api.descope.com";

  private final Config config;
  private final ManagementService managementService;
  private final AuthenticationService authenticationService;

  public DescopeClient() throws DescopeException {
    var descopeClient = new DescopeClient(new Config());
    this.config = descopeClient.config;
    this.managementService = descopeClient.managementService;
    this.authenticationService = descopeClient.authenticationService;
  }

  public DescopeClient(Config config) throws DescopeException {
    if (Objects.isNull(config)) {
      throw ServerCommonException.invalidArgument("Config");
    }

    String projectId = config.initializeProjectId();
    if (StringUtils.isBlank(projectId)) {
      throw ClientSetupException.missingProjectId();
    }

    String publicKey = config.initializePublicKey();
    if (StringUtils.isNotBlank(publicKey)) {
      log.info("Provided public key is set, forcing only provided public key validation");
    }
    config.initializeManagementKey();

    var client = getClient(config);
    this.authenticationService = getAuthenticationService(config, client);
    this.managementService = getManagementService(config, projectId, client);
    this.config = config;
  }

  private static ManagementService getManagementService(
      Config config, String projectId, Client client) {
    var managementParams =
        ManagementParams.builder()
            .projectId(projectId)
            .managementKey(config.getManagementKey())
            .build();
    return ManagementServiceBuilder.buildService(client, managementParams);
  }

  private static AuthenticationService getAuthenticationService(Config config, Client client) {
    var authParams =
        AuthParams.builder()
            .projectId(config.getProjectId())
            .publicKey(config.getPublicKey())
            .sessionJwtViaCookie(config.isSessionJWTViaCookie())
            .cookieDomain(config.getSessionJWTCookieDomain())
            .build();
    return AuthenticationServiceBuilder.buildService(client, authParams);
  }

  private static Client getClient(Config config) {
    var clientParams =
        ClientParams.builder()
            .projectId(config.getProjectId())
            .baseUrl(config.getDescopeBaseUrl())
            .customDefaultHeaders(config.getCustomDefaultHeaders())
            .build();
    return getClient(clientParams);
  }

  private static Client getClient(ClientParams params) {
    Map<String, String> customDefaultHeaders = params.getCustomDefaultHeaders();
    Map<String, String> defaultHeaders =
        Collections.isEmpty(customDefaultHeaders)
            ? new HashMap<>()
            : new HashMap<>(customDefaultHeaders);

    if (StringUtils.isBlank(params.getBaseUrl())) {
      params.setBaseUrl(DEFAULT_BASE_URL);
    }

    var sdkInfo = getSdkInfo();
    return Client.builder()
        .uri(params.getBaseUrl())
        .params(params)
        .headers(defaultHeaders)
        .sdkInfo(sdkInfo)
        .build();
  }

  private static SdkInfo getSdkInfo() {
    String name = "java";
    var javaVersion = Runtime.version();
    // TODO - Version & SHA | 10/04/23 | by keshavram
    return SdkInfo.builder().name(name).javaVersion(javaVersion.toString()).build();
  }
}
