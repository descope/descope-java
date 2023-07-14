package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "user-delete", description = "Delete a Descope user")
public class UserDelete extends UserBase implements Callable<Integer>{

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var userService = client.getManagementServices().getUserService();
      userService.delete(loginId);
      System.out.printf("User %s deleted\n", loginId);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
