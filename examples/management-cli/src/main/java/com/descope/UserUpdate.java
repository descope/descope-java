package com.descope;

import java.util.concurrent.Callable;
import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.user.request.UserRequest;
import picocli.CommandLine.Command;

@Command(name = "user-update", description = "Update a Descope user with validated email and phone")
public class UserUpdate extends UserChangeBase implements Callable<Integer>{

  @Override
  public Integer call() {
    int exitCode = 0;
    try {
      var client = new DescopeClient();
      var userService = client.getManagementServices().getUserService();
      UserRequest req = UserRequest.builder()
          .displayName(name)
          .email(email)
          .phone(phone)
          .verifiedEmail(true)
          .verifiedPhone(true)
          .invite(false)
          .build();
      userService.update(loginId, req);
      System.out.printf("User %s updated\n", loginId);
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
