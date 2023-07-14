package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "permission-load-all", description = "List all available permissions")
public class PermissionLoadAll extends HelpBase implements Callable<Integer>{

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var permissionService = client.getManagementServices().getPermissionService();
      var res = permissionService.loadAll();
      ObjectMapper objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
