package com.descope;

import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.UserResponse;
import lombok.Builder;

import java.util.List;

@Builder
final class CLIService {
    private ManagementServices managementServices;

    public void createUser(String loginId, UserRequest userRequest) {
        var userService = managementServices.getUserService();
        var userResponse = userService.create(loginId, userRequest);
        String userId = userResponse.getUserId();
        System.out.printf("User with userId: %s was successfully created%n", userId);
    }

    public void updateUser(String loginId, UserRequest userRequest) {
        var userService = managementServices.getUserService();
        var userResponse = userService.update(loginId, userRequest);
        String userId = userResponse.getUserId();
        System.out.printf("User with userId: %s was successfully updated%n", userId);
    }

    public void deleteUser(String loginId) {
        var userService = managementServices.getUserService();
        userService.delete(loginId);
        System.out.printf("User with loginId: %s was successfully deleted%n", loginId);
    }

    public void loadUser(String loginId) {
        var userService = managementServices.getUserService();
        UserResponse response = userService.load(loginId);
        System.out.printf("User with loginId: %s was successfully loaded%n", loginId);
        System.out.println(response);
    }

    public void searchAllUsers() {
        var userService = managementServices.getUserService();
        List<UserResponse> response = userService.searchAll(null);
        System.out.println(response);
    }
}
