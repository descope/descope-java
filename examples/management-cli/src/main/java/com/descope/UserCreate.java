package com.descope;

import com.descope.client.DescopeClient;
import com.descope.exception.DescopeException;
import com.descope.model.user.request.UserRequest;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "user-create", description = "Create a Descope user with validated email and phone")
public class UserCreate extends UserChangeBase implements Callable<Integer> {

  @Option(names = { "-t", "--test"}, description = "if provided, this will be a test user")
  boolean test;

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
      if (test) {
        var res = userService.createTestUser(loginId, req);
        System.out.printf("Test user %s created with user id %s\n", loginId, res.getUser().getUserId());
      } else {
        var res = userService.create(loginId, req);
        System.out.printf("User %s created with user id %s\n", loginId, res.getUser().getUserId());
      }
    } catch (DescopeException de) {
      exitCode = 1;
      System.err.println(String.format("%s - %s", de.getCode(), de.getMessage()));
    }
    return exitCode;
  }
}
