package com.descope;

import java.util.concurrent.Callable;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import picocli.CommandLine.Command;

@Command(name = "tenant-delete", description = "Delete a Descope tenant")
public class TenantDelete extends TenantBase implements Callable<Integer> {

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var tenantService = client.getManagementServices().getTenantService();
      tenantService.delete(tenantId);
      System.out.printf("Tenant %s deleted\n", tenantId);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
