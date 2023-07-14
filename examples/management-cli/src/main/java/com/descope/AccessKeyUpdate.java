package com.descope;

import java.util.concurrent.Callable;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "access-key-update", description = "Update a Descope access key")
public class AccessKeyUpdate extends AccessKeyBase implements Callable<Integer>{

  @Option(names = { "-n", "--name"}, description = "Access key name")
  String name;

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var keyService = client.getManagementServices().getAccessKeyService();
      keyService.update(keyId, name);
      System.out.printf("Key %s updated\n", name);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
