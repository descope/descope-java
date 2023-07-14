package com.descope;

import java.util.List;
import java.util.concurrent.Callable;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "tenant-update", description = "Update a Descope tenant")
public class TenantUpdate extends TenantBase implements Callable<Integer>{

  @Option(names = { "-n", "--name"}, description = "Tenant name")
  String name;
  @Option(names = { "-d", "--domain"}, description = "Self provisioned domains. Multiple are supported.")
  List<String> selfProvisionedDomains;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var tenantService = client.getManagementServices().getTenantService();
      tenantService.update(tenantId, name, selfProvisionedDomains);
      System.out.printf("Tenant %s [%s] updated\n", name, tenantId);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
