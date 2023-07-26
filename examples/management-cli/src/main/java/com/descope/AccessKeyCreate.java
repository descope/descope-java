package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AssociatedTenant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "access-key-create", description = "Create a Descope access key")
public class AccessKeyCreate extends HelpBase implements Callable<Integer> {

  @Option(names = { "-n", "--name"}, description = "Access key name")
  String name;
  @Option(names = { "-e", "--expiration"}, description = "Number of days this key is valid")
  int expiration;
  @Option(names = { "-r", "--role"}, description = "Optional role for the access key. Multiple can be provided.")
  List<String> roles;
  @Option(names = { "-t", "--tenant"}, description = "Optional tenant-roles pairs. Each is tenantId:role1,role2,role3")
  List<String> tenants;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var keyService = client.getManagementServices().getAccessKeyService();
      var associatedTenants = new ArrayList<AssociatedTenant>();
      if (tenants != null) {
        for (var t : tenants) {
          String[] parts = t.split(":");
          if (parts.length == 2) {
            String[] roles = parts[1].split(",");
            if (roles.length > 0) {
              associatedTenants.add(
                  AssociatedTenant.builder().tenantId(parts[0]).roleNames(Arrays.asList(roles)).build());
            }
          }
        }
      }
      var res = keyService.create(name, expiration, roles, associatedTenants);
      System.out.printf("Key %s created with id %s\n", name, res.getKey().getId());
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
