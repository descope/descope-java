package com.descope;

import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.UserRequest;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import org.apache.commons.lang3.StringUtils;

public class ManagementCLI {

  public static void main(String[] args) {
    final String DESCOPE_BASE_URL = System.getenv("DESCOPE_BASE_URL");
    final String DESCOPE_PROJECT_ID = System.getenv("DESCOPE_PROJECT_ID");
    final String DESCOPE_MANAGEMENT_KEY = System.getenv("DESCOPE_MANAGEMENT_KEY");

    if (StringUtils.isAnyBlank(DESCOPE_PROJECT_ID, DESCOPE_MANAGEMENT_KEY, DESCOPE_BASE_URL)) {
      throw new UnsupportedOperationException("Project ID / Management ID / Base URL is not set");
    }

    var client = Client.builder().uri(DESCOPE_BASE_URL).build();
    var managementParams =
        ManagementParams.builder()
            .projectId(DESCOPE_PROJECT_ID)
            .managementKey(DESCOPE_MANAGEMENT_KEY)
            .build();
    var managementServices = ManagementServiceBuilder.buildServices(client, managementParams);
    var cliService = CLIService.builder().managementServices(managementServices).build();

    String operation = args[0];
    switch (operation) {
      case "user-create" -> {
        String loginId = System.getProperty("loginId");
        String email = System.getProperty("email");
        String phone = System.getProperty("phone");
        String name = System.getProperty("name");

        if (StringUtils.isAnyBlank(loginId, email, phone, name)) {
          throw new IllegalArgumentException(
              "Login ID, Email, Phone and Name are Required. Please pass these arguments as `-D<key>=<value>`");
        }

        var userRequest =
            UserRequest.builder()
                .loginId(loginId)
                .email(email)
                .phone(phone)
                .displayName(name)
                .build();
        cliService.createUser(loginId, userRequest);
      }

      case "user-update" -> {
        String loginId = System.getProperty("loginId");
        String email = System.getProperty("email");
        String phone = System.getProperty("phone");
        String name = System.getProperty("name");

        if (StringUtils.isAnyBlank(loginId, email, phone, name)) {
          throw new IllegalArgumentException(
              "Login ID, Email, Phone and Name are Required. Please pass these arguments as `-D<key>=<value>`");
        }

        var userRequest =
            UserRequest.builder()
                .loginId(loginId)
                .email(email)
                .phone(phone)
                .displayName(name)
                .build();
        cliService.updateUser(loginId, userRequest);
      }

      case "user-delete" -> {
        String loginId = System.getProperty("loginId");

        if (StringUtils.isAnyBlank(loginId)) {
          throw new IllegalArgumentException(
              "Login ID is Required. Please pass these arguments as `-D<key>=<value>`");
        }
        cliService.deleteUser(loginId);
      }

      case "user-load" -> {
        String loginId = System.getProperty("loginId");

        if (StringUtils.isAnyBlank(loginId)) {
          throw new IllegalArgumentException(
              "Login ID is Required. Please pass these arguments as `-D<key>=<value>`");
        }
        cliService.loadUser(loginId);
      }
      case "user-search-all" -> cliService.searchAllUsers();

      default -> throw new UnsupportedOperationException(
          "Invalid Operation %s".formatted(operation));
    }
  }
}
