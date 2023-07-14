package com.descope;

import java.util.concurrent.Callable;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "permission-create", description = "Create a Descope permission")
public class PermissionCreate extends PermissionBase implements Callable<Integer>{

  @Option(names = { "-d", "--description"}, description = "Permission description")
  String desc;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var permissionService = client.getManagementServices().getPermissionService();
      permissionService.create(name, desc);
      System.out.printf("Permission %s created\n", name);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
