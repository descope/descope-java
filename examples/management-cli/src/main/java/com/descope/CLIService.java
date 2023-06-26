package com.descope;

import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.sdk.mgmt.UserService;
import lombok.Builder;

@Builder
final class CLIService {
  private ManagementServices managementServices;

  public void createUser(String loginId, UserRequest userRequest) {
    var userService = managementServices.getUserService();
    userService.create(loginId, userRequest);
  }
}
