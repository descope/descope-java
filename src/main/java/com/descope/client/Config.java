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
/* Configuration struct describes the configuration data for the authentication methods. */
public class Config {
  // ProjectID (required, "") - used to validate and authenticate against descope services.
  private String projectId;

  // PublicKey (optional, "") - used to provide a management key that's required for using any of
  // the Management APIs. If empty, this value is retrieved from the DESCOPE_MANAGEMENT_KEY
  // environment variable instead. If neither values are set then any Management API call with
  // fail.
  private String managementKey;

  // PublicKey (optional, "") - used to override or implicitly use a dedicated public key in order
  // to decrypt and validate the JWT tokens during ValidateSessionRequest(). If empty, will attempt
  // to fetch all public keys from the specified project id.
  private String publicKey;

  // DescopeBaseURL (optional, "https://api.descope.com") - override the default base URL used to
  // communicate with descope services.
  private String descopeBaseUrl;

  // CustomDefaultHeaders (optional, nil) - add custom headers to all requests used to communicate
  // with descope services.
  private Map<String, String> customDefaultHeaders;

  // State whether session jwt should be sent to client in cookie or let the calling function handle
  // the transfer of the jwt, defaults to leaving it for calling function, use cookie if session jwt
  // will stay small (less than 1k) session cookie can grow bigger, in case of using authorization,
  // or adding custom claims
  private boolean sessionJWTViaCookie;

  // When using cookies, set the cookie domain here. Alternatively this can be done via the Descope
  // console.
  private String sessionJWTCookieDomain;

  public String initializeProjectId() {
    if (StringUtils.isBlank(this.projectId)) {
      this.projectId = EnvironmentUtils.getProjectId();
    }
    return this.projectId;
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
}
