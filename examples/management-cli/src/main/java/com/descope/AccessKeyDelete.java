package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "access-key-delete", description = "Delete a Descope access key")
public class AccessKeyDelete extends AccessKeyBase implements Callable<Integer>{

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var keyService = client.getManagementServices().getAccessKeyService();
      keyService.delete(keyId);
      System.out.printf("Key %s deleted\n", keyId);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
