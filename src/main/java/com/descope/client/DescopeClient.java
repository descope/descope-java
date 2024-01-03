package com.descope.client;

import com.descope.exception.ClientSetupException;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.SigningKey;
import com.descope.model.mgmt.ManagementServices;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.auth.impl.KeyProvider;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Collections;
import java.util.HashMap;
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
      log.debug("Provided public key is set, forcing only provided public key validation");
    }
    config.initializeManagementKey();
    config.initializeBaseURL();

    Client client = getClient(config);
    this.authenticationServices = AuthenticationServiceBuilder.buildServices(client);
    this.managementServices = ManagementServiceBuilder.buildServices(client);
    this.config = config;
  }

  private static Client getClient(Config config) {
    SdkInfo sdkInfo = getSdkInfo();
    Client c = Client.builder()
        .uri(StringUtils.isBlank(config.getDescopeBaseUrl()) ? DEFAULT_BASE_URL : config.getDescopeBaseUrl())
        .projectId(config.getProjectId())
        .managementKey(config.getManagementKey())
        .headers(
            Collections.isEmpty(config.getCustomDefaultHeaders())
              ? new HashMap<>() : new HashMap<>(config.getCustomDefaultHeaders()))
        .sdkInfo(sdkInfo)
        .build();
    if (StringUtils.isNotBlank(config.getPublicKey())) {
      final ObjectMapper objectMapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      try {
        SigningKey sk = objectMapper.readValue(config.getPublicKey(), SigningKey.class);
        c.setProvidedKey(KeyProvider.getPublicKey(sk));
      } catch (Exception e) {
        throw ServerCommonException.invalidSigningKey(e.getMessage());
      }
    }
    return c;
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
