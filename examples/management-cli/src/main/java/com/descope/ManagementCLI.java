package com.descope;

import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.UserRequest;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

public class ManagementCLI {

  public static void main(String[] args) {
    final String DESCOPE_BASE_URL = System.getenv("DESCOPE_BASE_URL");
    final String DESCOPE_PROJECT_ID = System.getenv("DESCOPE_PROJECT_ID");
    final String DESCOPE_MANAGEMENT_KEY = System.getenv("DESCOPE_MANAGEMENT_KEY");

    if (StringUtils.isAnyBlank(DESCOPE_PROJECT_ID, DESCOPE_MANAGEMENT_KEY)) {
      throw new UnsupportedOperationException("Project ID / Management ID is not set");
    }

    Client.ClientBuilder clientBuilder = Client.builder();
    if (StringUtils.isNoneBlank(DESCOPE_BASE_URL)) {
      clientBuilder.uri(DESCOPE_BASE_URL);
    }
    var client = clientBuilder.build();

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
        Options options = new Options();
        options.addOption(getOption("loginId", true, "User login id"));
        options.addOption(getOption("email", true, "User email address"));
        options.addOption(getOption("name", true, "User name"));
        options.addOption(getOption("phone", true, "USer phone number"));

        CommandLine cmd = getCmdParser(options, args);

        String loginId = cmd.getOptionValue("loginId");
        String email = cmd.getOptionValue("email");
        String phone = cmd.getOptionValue("phone");
        String name = cmd.getOptionValue("name");

        var userRequest =
            UserRequest.builder()
                .loginId(loginId)
                .email(email)
                .phone(phone)
                .displayName(name)
                .roleNames(Collections.emptyList())
                .tenants(Collections.emptyList())
                .customAttributes(Collections.emptyMap())
                .picture("")
                .build();
        cliService.createUser(loginId, userRequest);
      }

      case "user-update" -> {
        Options options = new Options();

        options.addOption(getOption("loginId", true, "User login id"));
        options.addOption(getOption("email", true, "User email address"));
        options.addOption(getOption("name", true, "User name"));
        options.addOption(getOption("phone", true, "USer phone number"));

        CommandLine cmd = getCmdParser(options, args);

        String loginId = cmd.getOptionValue("loginId");
        String email = cmd.getOptionValue("email");
        String phone = cmd.getOptionValue("phone");
        String name = cmd.getOptionValue("name");

        var userRequest =
            UserRequest.builder()
                .loginId(loginId)
                .email(email)
                .phone(phone)
                .displayName(name)
                .roleNames(Collections.emptyList())
                .tenants(Collections.emptyList())
                .customAttributes(Collections.emptyMap())
                .picture("")
                .invite(false)
                .test(false)
                .build();
        cliService.updateUser(loginId, userRequest);
      }

      case "user-delete" -> {
        Options options = new Options();

        options.addOption(getOption("loginId", true, "User login id"));

        CommandLine cmd = getCmdParser(options, args);

        String loginId = cmd.getOptionValue("loginId");

        cliService.deleteUser(loginId);
      }

      case "user-load" -> {
        Options options = new Options();

        options.addOption(getOption("loginId", true, "User login id"));

        CommandLine cmd = getCmdParser(options, args);

        String loginId = cmd.getOptionValue("loginId");

        cliService.loadUser(loginId);
      }
      case "user-search-all" -> cliService.searchAllUsers();
        // TODO: Testing pending as not getting success response for empty tenants
      case "access-key-create" -> {
        Options options = new Options();

        options.addOption(getOption("keyName", true, "Access Key Name"));
        options.addOption(getOption("expirationTime", true, "Access Key ExpirationTime"));

        CommandLine cmd = getCmdParser(options, args);

        String keyName = cmd.getOptionValue("keyName");
        String expirationTime = cmd.getOptionValue("expirationTime");

        AssociatedTenant associatedTenant = new AssociatedTenant();
        associatedTenant.setRoleNames(Collections.emptyList());
        associatedTenant.setTenantId("");
        cliService.createAccessKey(
            keyName, expirationTime, Collections.emptyList(), List.of(associatedTenant));
      }
      case "access-key-update" -> {
        Options options = new Options();

        options.addOption(getOption("keyId", true, "AccessKey Id"));
        options.addOption(getOption("keyName", true, "AccessKey Name"));

        CommandLine cmd = getCmdParser(options, args);

        String keyId = cmd.getOptionValue("keyId");
        String keyName = cmd.getOptionValue("keyName");

        cliService.updateAccessKey(keyId, keyName);
      }
      case "access-key-delete" -> {
        Options options = new Options();

        options.addOption(getOption("keyId", true, "Access Key Id"));

        CommandLine cmd = getCmdParser(options, args);

        String keyId = cmd.getOptionValue("keyId");

        cliService.deleteAccessKey(keyId);
      }
      case "access-key-load" -> {
        Options options = new Options();

        options.addOption(getOption("keyId", true, "AccessKey Id"));

        CommandLine cmd = getCmdParser(options, args);

        String keyId = cmd.getOptionValue("keyId");

        cliService.loadAccessKey(keyId);
      }
      case "access-key-search-all" -> cliService.searchAllAccessKey();

      case "tenant-create" -> {
        Options options = new Options();

        options.addOption(getOption("tenantName", true, "Tenant Name"));

        CommandLine cmd = getCmdParser(options, args);

        String tenantName = cmd.getOptionValue("tenantName");

        cliService.createTenant(tenantName);
      }
      case "tenant-update" -> {
        Options options = new Options();

        options.addOption(getOption("tenantId", true, "TenantId"));
        options.addOption(getOption("tenantName", true, "TenantName"));

        CommandLine cmd = getCmdParser(options, args);

        String tenantId = cmd.getOptionValue("tenantId");
        String tenantName = cmd.getOptionValue("tenantName");

        cliService.updateTenant(tenantId, tenantName);
      }
      case "tenant-delete" -> {
        Options options = new Options();

        options.addOption(getOption("tenantId", true, "TenantId"));

        CommandLine cmd = getCmdParser(options, args);

        String tenantId = cmd.getOptionValue("tenantId");

        cliService.deleteTenant(tenantId);
      }
      case "tenant-search-all" -> cliService.searchAllTenant();

      case "group-all-for-tenant" -> {
        Options options = new Options();

        options.addOption(getOption("tenantId", true, "Group TenantId"));

        CommandLine cmd = getCmdParser(options, args);

        String tenantId = cmd.getOptionValue("tenantId");

        cliService.groupAllForTenant(tenantId);
      }

      case "group-all-for-member" -> {
        Options options = new Options();

        options.addOption(getOption("tenantId", true, "GroupTenantId"));
        options.addOption(getOption("userIds", true, "GroupUserIds"));
        options.addOption(getOption("loginIds", true, "GroupLoginIds"));

        CommandLine cmd = getCmdParser(options, args);

        String tenantId = cmd.getOptionValue("tenantId");
        String userIds = cmd.getOptionValue("userIds");
        String loginIds = cmd.getOptionValue("loginIds");

        cliService.groupAllForMember(tenantId, userIds, loginIds);
      }
      case "group-members" -> {
        Options options = new Options();

        options.addOption(getOption("tenantId", true, "Group TenantId"));
        options.addOption(getOption("groupId", true, "Group Id"));

        CommandLine cmd = getCmdParser(options, args);

        String tenantId = cmd.getOptionValue("tenantId");
        String groupId = cmd.getOptionValue("groupId");

        cliService.groupMembers(tenantId, groupId);
      }

      case "permission-create" -> {
        Options options = new Options();

        options.addOption(getOption("permissionName", true, "PermissionName"));

        CommandLine cmd = getCmdParser(options, args);

        String permissionName = cmd.getOptionValue("permissionName");

        cliService.createPermission(permissionName);
      }
      case "permission-update" -> {
        Options options = new Options();

        options.addOption(getOption("permissionName", true, "PermissionName"));
        options.addOption(getOption("permissionId", true, "PermissionId"));

        CommandLine cmd = getCmdParser(options, args);

        String permissionName = cmd.getOptionValue("permissionName");
        String permissionId = cmd.getOptionValue("permissionId");

        cliService.updatePermission(permissionName, permissionId);
      }
      case "permission-delete" -> {
        Options options = new Options();

        options.addOption(getOption("permissionId", true, "PermissionId"));

        CommandLine cmd = getCmdParser(options, args);

        String permissionId = cmd.getOptionValue("permissionId");

        cliService.deletePermission(permissionId);
      }
      case "permission-all" -> cliService.permissionAll();

      case "role-create" -> {
        Options options = new Options();

        options.addOption(getOption("roleName", true, "RoleName"));

        CommandLine cmd = getCmdParser(options, args);

        String roleName = cmd.getOptionValue("roleName");

        cliService.createRole(roleName);
      }
      case "role-update" -> {
        Options options = new Options();

        options.addOption(getOption("roleName", true, "RoleName"));
        options.addOption(getOption("roleId", true, "RoleId"));

        CommandLine cmd = getCmdParser(options, args);

        String roleName = cmd.getOptionValue("roleName");
        String roleId = cmd.getOptionValue("roleId");

        cliService.updateRole(roleId, roleName);
      }
      case "role-delete" -> {
        Options options = new Options();

        options.addOption(getOption("roleId", true, "RoleId"));

        CommandLine cmd = getCmdParser(options, args);

        String roleId = cmd.getOptionValue("roleId");

        cliService.deleteRole(roleId);
      }
      case "role-all" -> cliService.roleAll();

      default -> throw new UnsupportedOperationException(
          "Invalid Operation %s".formatted(operation));
    }
  }

  private static Option getOption(String option, boolean hasArg, String desc) {
    Option newOption = new Option(option, hasArg, desc);
    newOption.setRequired(true);
    return newOption;
  }

  private static CommandLine getCmdParser(Options options, String[] args) {
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);

      System.exit(1);
    }
    return cmd;
  }
}
