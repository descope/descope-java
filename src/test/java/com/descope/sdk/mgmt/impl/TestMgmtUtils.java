package com.descope.sdk.mgmt.impl;


import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.utils.EnvironmentUtils;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestMgmtUtils {
  static ManagementParams getManagementParams() {
    return ManagementParams.builder()
        .projectId(EnvironmentUtils.getProjectId())
        .managementKey(EnvironmentUtils.getManagementKey())
        .build();
  }

  static Client getClient() {
    return Client.builder().uri("https://api.descope.com").build();
  }

  static String getRandomName(String prefix) {
    return prefix + UUID.randomUUID().toString();
  }
}
