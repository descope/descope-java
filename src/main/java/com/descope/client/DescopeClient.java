package com.descope.client;

import com.descope.exception.ClientSetupException;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import com.descope.model.client.ClientParams;
import com.descope.model.client.SdkInfo;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.mgmt.ManagementServices;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import io.jsonwebtoken.lang.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Getter
public class DescopeClient {

  private static final String DEFAULT_BASE_URL = "https://api.descope.com";

  private final Config config;
  private final ManagementServices managementServices;
  private final AuthenticationServices authenticationServices;

  public DescopeClient() throws DescopeException {
    this(new Config());
  }

  public DescopeClient(Config config) throws DescopeException {
    if (config == null) {
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
    config.initializeBaseURL();

    Client client = getClient(config);
    this.authenticationServices = getAuthenticationServices(config, client);
    this.managementServices = getManagementServices(config, projectId, client);
    this.config = config;
  }

  private static ManagementServices getManagementServices(
      Config config, String projectId, Client client) {
    ManagementParams managementParams = ManagementParams.builder()
        .projectId(projectId)
        .managementKey(config.getManagementKey())
        .build();
    return ManagementServiceBuilder.buildServices(client, managementParams);
  }

  private static AuthenticationServices getAuthenticationServices(Config config, Client client) {
    AuthParams authParams = AuthParams.builder()
        .projectId(config.getProjectId())
        .publicKey(config.getPublicKey())
        .sessionJwtViaCookie(config.isSessionJWTViaCookie())
        .cookieDomain(config.getSessionJWTCookieDomain())
        .build();
    return AuthenticationServiceBuilder.buildServices(client, authParams);
  }

  private static Client getClient(Config config) {
    ClientParams clientParams = ClientParams.builder()
        .projectId(config.getProjectId())
        .baseUrl(config.getDescopeBaseUrl())
        .customDefaultHeaders(config.getCustomDefaultHeaders())
        .build();
    return getClient(clientParams);
  }

  private static Client getClient(ClientParams params) {
    Map<String, String> customDefaultHeaders = params.getCustomDefaultHeaders();
    Map<String, String> defaultHeaders = Collections.isEmpty(customDefaultHeaders)
        ? new HashMap<>()
        : new HashMap<>(customDefaultHeaders);

    if (StringUtils.isBlank(params.getBaseUrl())) {
      params.setBaseUrl(DEFAULT_BASE_URL);
    }

    SdkInfo sdkInfo = getSdkInfo();
    return Client.builder()
        .uri(params.getBaseUrl())
        .params(params)
        .headers(defaultHeaders)
        .sdkInfo(sdkInfo)
        .build();
  }

  private static SdkInfo getSdkInfo() {
    String name = "java";
    String version = System.getProperty("java.version");

    // TODO - SHA
    return SdkInfo.builder()
        .name(name)
        .javaVersion(version)
        .version(new SdkInfo().getClass().getPackage().getImplementationVersion())
        .build();
  }
}
