package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "permission-delete", description = "Delete a Descope permission")
public class PermissionDelete extends PermissionBase implements Callable<Integer> {

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var permissionService = client.getManagementServices().getPermissionService();
      permissionService.delete(name);
      System.out.printf("Permission %s deleted\n", name);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
