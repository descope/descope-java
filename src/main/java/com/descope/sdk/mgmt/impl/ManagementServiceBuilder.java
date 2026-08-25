package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LICENSE_LINK;

import com.descope.model.client.Client;
import com.descope.model.license.LicenseResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.utils.UriUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.Timeout;

@Slf4j
@UtilityClass
public class ManagementServiceBuilder {
  // The handshake sits on the client construction path, so it gets a hard ceiling and no retries.
  // Going through ApiProxy instead would inherit its retry ladder, which can add over ten seconds
  // of sleeps to every DescopeClient construction when the endpoint is degraded.
  private static final Timeout LICENSE_HANDSHAKE_TIMEOUT = Timeout.ofSeconds(5);

  public static ManagementServices buildServices(Client client) {
    fetchRateLimitTier(client);
    return ManagementServices.builder()
        .ssoService(new SsoServiceImpl(client))
        .jwtService(new JwtServiceImpl(client))
        .userService(new UserServiceImpl(client))
        .flowService(new FlowServiceImpl(client))
        .rolesService(new RolesServiceImpl(client))
        .groupService(new GroupServiceImpl(client))
        .tenantService(new TenantServiceImpl(client))
        .accessKeyService(new AccessKeyServiceImpl(client))
        .permissionService(new PermissionServiceImpl(client))
        .auditService(new AuditServiceImpl(client))
        .authzService(new AuthzServiceImpl(client))
        .fgaService(new FGAServiceImpl(client))
        .projectService(new ProjectServiceImpl(client))
        .passwordSettingsService(new PasswordSettingsServiceImpl(client))
        .ssoApplicationService(new SsoApplicationServiceImpl(client))
        .outboundAppsService(new OutboundAppsServiceImpl(client))
        .outboundAppsByTokenService(new OutboundAppsByTokenServiceImpl(client))
        .inboundAppsService(new InboundAppsServiceImpl(client))
        .userCustomAttributesService(new UserCustomAttributesServiceImpl(client))
        .build();
  }

  // Best effort: a failure here only means management requests go out without the tier header,
  // which the edge treats as the lowest bucket. It must never stop the SDK from being built.
  static void fetchRateLimitTier(Client client) {
    if (StringUtils.isBlank(client.getManagementKey())) {
      return;
    }
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(LICENSE_HANDSHAKE_TIMEOUT)
        .setResponseTimeout(LICENSE_HANDSHAKE_TIMEOUT)
        .build();
    // disableAutomaticRetries is deliberate: httpclient5 otherwise retries 429 and 503 on its own,
    // which puts an unbounded-ish wait back on the construction path this timeout exists to bound.
    try (CloseableHttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .disableAutomaticRetries()
        .build()) {
      String authHeader =
          String.format("Bearer %s:%s", client.getProjectId(), client.getManagementKey());
      String body = httpClient.execute(
          ClassicRequestBuilder.get(UriUtils.getUri(client.getUri(), MANAGEMENT_LICENSE_LINK))
              .addHeader("Authorization", authHeader)
              .addHeader("Content-Type", "application/json")
              .build(),
          response -> {
            if (response.getCode() != 200 || response.getEntity() == null) {
              log.warn("License handshake returned status {}", response.getCode());
              return null;
            }
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
          });
      if (StringUtils.isBlank(body)) {
        return;
      }
      LicenseResponse licenseResponse = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .readValue(body, LicenseResponse.class);
      if (licenseResponse != null && StringUtils.isNotBlank(licenseResponse.getRateLimitTier())) {
        client.setRateLimitTier(licenseResponse.getRateLimitTier());
        log.debug("Rate limit tier fetched: {}", licenseResponse.getRateLimitTier());
      }
    } catch (Exception e) {
      log.warn("Failed to fetch rate limit tier, continuing without license header: {}",
          e.getMessage());
    }
  }
}
