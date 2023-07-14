package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "role-create", description = "Create a Descope role")
public class RoleCreate extends RoleBase implements Callable<Integer>{

  @Option(names = { "-d", "--description"}, description = "Role description")
  String desc;
  @Option(names = { "-p", "--permission"}, description = "Permission associated with role. Multiple are supported.")
  List<String> permissions;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var roleService = client.getManagementServices().getRolesService();
      roleService.create(name, desc, permissions);
      System.out.printf("Role %s created\n", name);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
