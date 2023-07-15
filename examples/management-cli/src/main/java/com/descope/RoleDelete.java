package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "role-delete", description = "Delete a Descope role")
public class RoleDelete extends RoleBase implements Callable<Integer> {

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var roleService = client.getManagementServices().getRolesService();
      roleService.delete(name);
      System.out.printf("Role %s deleted\n", name);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
