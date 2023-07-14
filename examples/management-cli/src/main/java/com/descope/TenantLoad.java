package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "tenant-load-all", description = "Load a Descope tenant")
public class TenantLoad extends HelpBase implements Callable<Integer> {

  @Override
  public Integer call() throws JsonProcessingException {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var tenantService = client.getManagementServices().getTenantService();
      var res = tenantService.loadAll();
      ObjectMapper objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res));
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
