package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "tenant-create", description = "Create a Descope tenant")
public class TenantCreate extends HelpBase implements Callable<Integer>{

  @Option(names = { "-i", "--tenantId" }, description = "Optional tenant id")
  String tenantId;
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
      if (StringUtils.isNotBlank(tenantId)) {
        tenantService.createWithId(tenantId, name, selfProvisionedDomains);
        System.out.printf("Tenant %s created with id %s\n", name, tenantId);
      } else {
        var res = tenantService.create(name, selfProvisionedDomains);
        System.out.printf("Tenant %s created with id %s\n", name, res);
      }
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
