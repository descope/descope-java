package com.descope.sdk.mgmt.impl;


import com.descope.model.mgmt.ManagementParams;
import com.descope.utils.EnvironmentUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestMgmtUtils {
  public static ManagementParams getManagementParams() {
    return ManagementParams.builder()
        .projectId(EnvironmentUtils.getProjectId())
        .managementKey(EnvironmentUtils.getManagementKey())
        .build();
  }
}
