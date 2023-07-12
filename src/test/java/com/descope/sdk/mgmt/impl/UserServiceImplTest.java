package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.AllUsersResponseDetails;
import com.descope.model.user.response.UserResponse;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
import java.net.URI;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class UserServiceImplTest {

  private final List<String> mockRoles = List.of("role1", "role2");
  private final String mockUrl = "http://localhost.com";
  private UserService userService;
  private TenantService tenantService;
  private RolesService roleService;

  @BeforeEach
  void setUp() {
    var authParams = TestMgmtUtils.getManagementParams();
    var client = TestMgmtUtils.getClient();
    var mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
    this.userService = mgmtServices.getUserService();
    this.tenantService = mgmtServices.getTenantService();
    this.roleService = mgmtServices.getRolesService();
  }

  @Test
  void testCreateForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.create("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testCreateTestUserForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.createTestUser("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testInviteForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.invite("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.update("", new UserRequest()));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.update("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testDeleteForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.delete(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      userService.delete("someLoginId");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testDeleteAllTestUsersForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      userService.deleteAllTestUsers();
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.load(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.load("someLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadByUserIdForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.loadByUserId(""));
    assertNotNull(thrown);
    assertEquals("The User ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadByUserIdForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.loadByUserId("SomeUserId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testActivateForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.activate(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testActivateForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.activate("someLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testDeactivateForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.deactivate(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeactivateForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.deactivate("someLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateEmailForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.updateEmail("", "someEmail", false));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateEmailForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.updateEmail("someLoginId", "someEmail", false);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdatePhoneForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.updatePhone("", "someEmail", false));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdatePhoneForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.updatePhone("someLoginId", "1234567890", false);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateDisplayNameForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.updateDisplayName("", "someDisplay"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateDisplayNameForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.updateDisplayName("someLoginId", "someDisplay");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdatePictureForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.updatePicture("", "somePicture"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdatePictureForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.updatePicture("someLoginId", "somePicture");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateCustomAttributesForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.updateCustomAttributes("", "someKey", 0));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateCustomAttributesForEmptyKey() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.updateCustomAttributes("someLoginId", "", 0));
    assertNotNull(thrown);
    assertEquals("The Key argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateCustomAttributesForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.updateCustomAttributes("someLoginId", "someKey", 0);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testAddRolesForEmptyKeyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.addRoles("", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddRolesForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.addRoles("someLoginId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testRemoveRolesForEmptyKeyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.removeRoles("", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRemoveRolesForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.removeRoles("someLoginId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testAddTenantForEmptyKeyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.addTenant("", "someTenantId"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddTenantForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.addTenant("someLoginId", "someTenantId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testRemoveTenantForEmptyKeyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.removeTenant("", "someTenantId"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRemoveTenantForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.removeTenant("someLoginId", "someTenantId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testAddTenantRolesForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.addTenantRoles("", "someTenantId", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddTenantRolesForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.addTenantRoles("someLoginId", "someTenantId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testRemoveTenantRolesForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.removeTenantRoles("", "someTenantId", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRemoveTenantRolesForSuccess() {
    var userResponseDetails = mock(UserResponseDetails.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.removeTenantRoles("someLoginId", "someTenantId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testSetPasswordForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.setPassword("", "somePassword"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSetPasswordForEmptyPassword() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.setPassword("someLoginId", ""));
    assertNotNull(thrown);
    assertEquals("The Password argument is invalid", thrown.getMessage());
  }

  @Test
  void testSetPasswordForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      userService.setPassword("someLoginId", "somePassword");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testExpirePasswordForEmpty() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> userService.expirePassword(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testExpirePasswordForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      userService.expirePassword("someLoginId");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testGenerateOtpForTestUserForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.generateOtpForTestUser("", DeliveryMethod.EMAIL));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateOtpForTestUserForSuccess() {
    var mockResponse = "12345";
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.generateOtpForTestUser("someLoginId", DeliveryMethod.EMAIL);
      Assertions.assertThat(response).isEqualTo("12345");
    }
  }

  @Test
  void testGenerateMagicLinkForTestUserForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                userService.generateMagicLinkForTestUser(
                    "", URI.create(mockUrl), DeliveryMethod.EMAIL));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateMagicLinkForTestUserForSuccess() {
    var mockResponse = "12345";
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response =
          userService.generateMagicLinkForTestUser(
              "someLoginId", URI.create(mockUrl), DeliveryMethod.EMAIL);
      Assertions.assertThat(response).isEqualTo("12345");
    }
  }

  @Test
  void testGenerateEnchantedLinkForTestUserForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.generateEnchantedLinkForTestUser("", URI.create(mockUrl)));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateEnchantedLinkForTestUserForSuccess() {
    var mockResponse = "12345";
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response =
          userService.generateEnchantedLinkForTestUser("someLoginId", URI.create(mockUrl));
      Assertions.assertThat(response).isEqualTo("12345");
    }
  }

  @Test
  void testSearchAllForSuccess() {
    var userResponse = mock(UserResponse.class);
    var allUsersResponse = new AllUsersResponseDetails(List.of(userResponse));
    var userSearchRequest = UserSearchRequest.builder().limit(6).page(1).build();
    var apiProxy = mock(ApiProxy.class);
    doReturn(allUsersResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.searchAll(userSearchRequest);
      Assertions.assertThat(response.getUsers().size()).isEqualTo(1);
    }
  }

  @Test
  void testSearchAllForInvalidLimit() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> userService.searchAll(new UserSearchRequest()));
    assertNotNull(thrown);
    assertEquals("The limit argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchAllForInvalidPage() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> userService.searchAll(UserSearchRequest.builder().limit(1).build()));
    assertNotNull(thrown);
    assertEquals("The page argument is invalid", thrown.getMessage());
  }

  @Test
  void testFunctionalFullCycle() {
    String loginId = TestMgmtUtils.getRandomName("u-");
    String email = TestMgmtUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    // Create
    var createResponse = userService.create(loginId,
        UserRequest.builder()
          .loginId(loginId)
          .email(email)
          .verifiedEmail(true)
          .phone(phone)
          .verifiedPhone(true)
          .displayName("Testing Test")
          .invite(false)
          .build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    Assertions.assertThat(user.getLoginIds()).contains(loginId);
    assertEquals(email, user.getEmail());
    assertEquals("+15555555555", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("invited", user.getStatus());
    // Disable
    var deactivateResponse = userService.deactivate(loginId);
    user = deactivateResponse.getUser();
    assertNotNull(user);
    assertEquals("disabled", user.getStatus());
    // Enable
    var activateResponse = userService.activate(loginId);
    user = activateResponse.getUser();
    assertNotNull(user);
    assertEquals("enabled", user.getStatus());
    // Update
    var updateResponse = userService.update(loginId,
        UserRequest.builder()
          .loginId(loginId)
          .email(email)
          .verifiedEmail(true)
          .phone(phone)
          .verifiedPhone(true)
          .displayName("Testing Test1")
          .invite(false)
          .build());
    user = updateResponse.getUser();
    assertNotNull(user);
    assertEquals("Testing Test1", user.getName());
    // Update individual fields
    userService.updateDisplayName(loginId, "Testing Test");
    email = TestMgmtUtils.getRandomName("test-") + "@descope.com";
    userService.updateEmail(loginId, email, true);
    userService.updatePhone(loginId, "+1-555-555-6666", true);
    var loadResponse = userService.load(loginId);
    user = loadResponse.getUser();
    assertNotNull(user);
    assertEquals(email, user.getEmail());
    assertEquals("+15555556666", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("enabled", user.getStatus());
    loadResponse = userService.loadByUserId(createResponse.getUser().getUserId());
    assertNotNull(user);
    assertEquals(email, user.getEmail());
    assertEquals("+15555556666", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("enabled", user.getStatus());
    var searchResponse = userService.searchAll(null);
    boolean found = false;
    for (var u : searchResponse.getUsers()) {
      if (u.getUserId().equals(createResponse.getUser().getUserId())) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    // Delete
    userService.delete(loginId);
  }

  @Test
  void testFunctionalTestUsers() {
    String loginId = TestMgmtUtils.getRandomName("u-");
    String email = TestMgmtUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    // Create
    var createResponse = userService.createTestUser(loginId,
        UserRequest.builder()
          .loginId(loginId)
          .email(email)
          .verifiedEmail(true)
          .phone(phone)
          .verifiedPhone(true)
          .displayName("Testing Test")
          .invite(false)
          .build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    Assertions.assertThat(user.getLoginIds()).contains(loginId);
    assertEquals(email, user.getEmail());
    assertEquals("+15555555555", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("invited", user.getStatus());
    assertEquals(true, user.getTest());
    var searchResponse = userService.searchAll(
        UserSearchRequest.builder().withTestUser(true).build());
    boolean found = false;
    for (var u : searchResponse.getUsers()) {
      if (u.getUserId().equals(createResponse.getUser().getUserId())) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    searchResponse = userService.searchAll(
        UserSearchRequest.builder().testUsersOnly(true).build());
    found = false;
    for (var u : searchResponse.getUsers()) {
      if (u.getUserId().equals(createResponse.getUser().getUserId())) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    // Delete
    userService.deleteAllTestUsers();
  }

  @Test
  void testFunctionalUserWithTenantAndRole() {
    String tenantName = TestMgmtUtils.getRandomName("t-");
    String tenantId = tenantService.create(tenantName, List.of(tenantName + ".com"));
    assertThat(tenantId).isNotBlank();
    String roleName = TestMgmtUtils.getRandomName("r-").substring(0, 20);
    roleService.create(roleName, "", null);
    String loginId = TestMgmtUtils.getRandomName("u-");
    String email = TestMgmtUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    // Create
    var createResponse = userService.create(loginId,
        UserRequest.builder()
          .loginId(loginId)
          .email(email)
          .verifiedEmail(true)
          .phone(phone)
          .verifiedPhone(true)
          .displayName("Testing Test")
          .invite(false)
          .userTenants(List.of(new AssociatedTenant(tenantId, List.of(roleName))))
          .build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    Assertions.assertThat(user.getLoginIds()).contains(loginId);
    assertEquals(email, user.getEmail());
    assertEquals("+15555555555", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("invited", user.getStatus());
    assertThat(user.getUserTenants()).containsExactly(
      new AssociatedTenant(tenantId, List.of(roleName)));
    // Delete
    userService.delete(loginId);
    tenantService.delete(tenantId);
    roleService.delete(roleName);
  }
}
