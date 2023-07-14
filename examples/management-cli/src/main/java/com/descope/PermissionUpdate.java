package com.descope;

import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "permission-update", description = "Update a Descope permission")
public class PermissionUpdate extends PermissionBase implements Callable<Integer>{

  @Option(names = { "-d", "--description"}, description = "Permission description")
  String desc;
  @Option(names = { "-u", "--newName"}, description = "The new name of the permission")
  String newName;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var permissionService = client.getManagementServices().getPermissionService();
      if (StringUtils.isBlank(newName)) {
        newName = name;
      }
      permissionService.update(name, newName, desc);
      System.out.printf("Permission %s updated with new name %s\n", name, newName);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
