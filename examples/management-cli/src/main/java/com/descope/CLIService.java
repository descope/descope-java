package com.descope;

import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import lombok.Builder;

@Builder
final class CLIService {
  private ManagementServices managementServices;

  public void createUser(String loginId, UserRequest userRequest) {
    var userService = managementServices.getUserService();
    var userResponse = userService.create(loginId, userRequest);
    String userId = userResponse.getUserId();
    System.out.printf("User with userId: %s was successfully created%n", userId);
  }
}
