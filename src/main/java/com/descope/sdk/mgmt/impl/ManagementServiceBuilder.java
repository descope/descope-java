package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LICENSE_LINK;

import com.descope.model.client.Client;
import com.descope.model.license.LicenseResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.utils.UriUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@UtilityClass
public class ManagementServiceBuilder {
  public static ManagementServices buildServices(Client client) {
    fetchLicense(client);
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

  private static void fetchLicense(Client client) {
    if (StringUtils.isBlank(client.getManagementKey())) {
      return;
    }
    try {
      String projectId = client.getProjectId();
      String managementKey = client.getManagementKey();
      ApiProxy apiProxy = ApiProxyBuilder.buildProxy(
          () -> String.format("Bearer %s:%s", projectId, managementKey), client);
      LicenseResponse response = apiProxy.get(
          UriUtils.getUri(client.getUri(), MANAGEMENT_LICENSE_LINK), LicenseResponse.class);
      if (response != null && StringUtils.isNotBlank(response.getLicenseType())) {
        client.setLicenseType(response.getLicenseType());
        log.debug("License type fetched: {}", response.getLicenseType());
      }
    } catch (Exception e) {
      log.warn("Failed to fetch license type, continuing without license header: {}", e.getMessage());
    }
  }
}
