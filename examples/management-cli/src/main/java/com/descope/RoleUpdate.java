package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "role-update", description = "Update a Descope role")
public class RoleUpdate extends RoleBase implements Callable<Integer>{

  @Option(names = { "-d", "--description"}, description = "Role description")
  String desc;
  @Option(names = { "-u", "--newName"}, description = "The new name of the role")
  String newName;
  @Option(names = { "-p", "--permission"}, description = "Permission associated with role. Multiple are supported.")
  List<String> permissions;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var roleService = client.getManagementServices().getRolesService();
      if (StringUtils.isBlank(newName)) {
        newName = name;
      }
      roleService.update(name, newName, desc, permissions);
      System.out.printf("Role %s updated with new name %s\n", name, newName);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
