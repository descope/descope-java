package com.descope.client;

import com.descope.utils.EnvironmentUtils;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
 * Configuration struct describes the configuration data for the authentication
 * methods.
 */
public class Config {
  // ProjectID (required, "") - used to validate and authenticate against descope
  // services.
  private String projectId;

  // PublicKey (optional, "") - used to provide a management key that's required
  // for using any of
  // the Management APIs. If empty, this value is retrieved from the
  // DESCOPE_MANAGEMENT_KEY
  // environment variable instead. If neither values are set then any Management
  // API call with
  // fail.
  private String managementKey;

  // AuthManagementKey (optional, "") - used to provide a management key to use
  // with Authentication APIs whose public access has been disabled.
  // If empty, this value is retrieved from the DESCOPE_AUTH_MANAGEMENT_KEY
  // environment variable instead. If neither values are set then any disabled
  // authentication methods API calls will fail.
  private String authManagementKey;

  // PublicKey (optional, "") - used to override or implicitly use a dedicated public key in order
  // to decrypt and validate the JWT tokens during ValidateSessionRequest().
  // If empty, will attempt to fetch all public keys from the specified project id.
  // Key should be a JSON in the format of com.descope.model.jwt.SigningKey
  private String publicKey;

  // DescopeBaseURL (optional, "https://api.descope.com") - override the default
  // base URL used to
  // communicate with descope services.
  private String descopeBaseUrl;

  // CustomDefaultHeaders (optional, nil) - add custom headers to all requests
  // used to communicate
  // with descope services.
  private Map<String, String> customDefaultHeaders;

  public String initializeProjectId() {
    if (StringUtils.isBlank(this.projectId)) {
      this.projectId = EnvironmentUtils.getProjectId();
    }
    return this.projectId;
  }

  public String initializeBaseURL() {
    if (StringUtils.isBlank(this.descopeBaseUrl)) {
      this.descopeBaseUrl = EnvironmentUtils.getBaseURL();
    }
    return this.descopeBaseUrl;
  }

  public String initializePublicKey() {
    if (StringUtils.isBlank(this.publicKey)) {
      this.publicKey = EnvironmentUtils.getPublicKey();
    }
    return this.publicKey;
  }

  public String initializeManagementKey() {
    if (StringUtils.isBlank(this.managementKey)) {
      this.managementKey = EnvironmentUtils.getManagementKey();
    }
    return this.managementKey;
  }

  public String initializeAuthManagementKey() {
    if (StringUtils.isBlank(this.authManagementKey)) {
      this.authManagementKey = EnvironmentUtils.getAuthManagementKey();
    }
    return this.authManagementKey;
  }
}
