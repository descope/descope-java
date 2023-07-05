package com.descope.sdk.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.net.URI;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class UserServiceImplTest {

  public static final String MOCK_PROJECT_ID = "someProjectId";
  private final List<String> mockRoles = List.of("role1", "role2");
  private final String mockUrl = "http://localhost.com";
  private UserService userService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.userService = ManagementServiceBuilder.buildServices(client, authParams).getUserService();
  }

  @Test
  void testCreateForSuccess() {
    var userResponse = mock(UserResponse.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.create("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testCreateTestUserForSuccess() {
    var userResponse = mock(UserResponse.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.createTestUser("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testInviteForSuccess() {
    var userResponse = mock(UserResponse.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var userRequest = mock(com.descope.model.user.request.UserRequest.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).get(any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).get(any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userResponse = mock(UserResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(userResponse).when(apiProxy).post(any(), any(), any());
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
    var userSearchRequest = UserSearchRequest.builder().limit(6).page(1).build();
    var apiProxy = mock(ApiProxy.class);
    doReturn(List.of(userResponse)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = userService.searchAll(userSearchRequest);
      Assertions.assertThat(response.size()).isEqualTo(1);
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
}
