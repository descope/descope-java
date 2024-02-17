package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.enums.BatchUserPasswordAlgorithm;
import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.auth.InviteOptions;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.BatchUserPasswordHashed;
import com.descope.model.user.request.BatchUserRequest;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.AllUsersResponseDetails;
import com.descope.model.user.response.EnchantedLinkTestUserResponse;
import com.descope.model.user.response.GenerateEmbeddedLinkResponse;
import com.descope.model.user.response.MagicLinkTestUserResponse;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.ProviderTokenResponse;
import com.descope.model.user.response.UserHistoryResponse;
import com.descope.model.user.response.UserResponse;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.model.user.response.UsersBatchResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.sdk.auth.PasswordService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class UserServiceImplTest {

  private final List<String> mockRoles = Arrays.asList("role1", "role2");
  private final String mockUrl = "http://localhost.com";
  private UserService userService;
  private TenantService tenantService;
  private RolesService roleService;
  private MagicLinkService magicLinkService;
  private AuthenticationService authenticationService;
  private PasswordService passwordService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.userService = mgmtServices.getUserService();
    this.tenantService = mgmtServices.getTenantService();
    this.roleService = mgmtServices.getRolesService();
    AuthenticationServices authServices = AuthenticationServiceBuilder.buildServices(client);
    this.magicLinkService = authServices.getMagicLinkService();
    this.authenticationService = authServices.getAuthService();
    this.passwordService = authServices.getPasswordService();
  }

  @Test
  void testCreateForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    UserRequest userRequest = mock(UserRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.create("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testCreateBatchForSuccess() {
    UsersBatchResponse usersBatchResponse = mock(UsersBatchResponse.class);
    BatchUserRequest userRequest = mock(BatchUserRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(usersBatchResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UsersBatchResponse response = userService.createBatch(Arrays.asList(userRequest));
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testCreateTestUserForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    UserRequest userRequest = mock(UserRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.createTestUser("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testInviteForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    UserRequest userRequest = mock(UserRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    InviteOptions inviteUrl = InviteOptions.builder().inviteUrl("https://mockUrl.com").build();
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());

    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.invite("someLoginId", userRequest, inviteUrl);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.update("", new UserRequest()));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    UserRequest userRequest = mock(UserRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.update("someLoginId", userRequest);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLogoutForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.logoutUser(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLogoutByUserIDForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.logoutUserByUserId(""));
    assertNotNull(thrown);
    assertEquals("The User ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.delete(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      userService.delete("someLoginId");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testDeleteAllTestUsersForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).delete(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      userService.deleteAllTestUsers();
      verify(apiProxy, times(1)).delete(any(), any(), any());
    }
  }

  @Test
  void testLoadForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.load(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.load("someLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadByUserIdForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.loadByUserId(""));
    assertNotNull(thrown);
    assertEquals("The User ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadByUserIdForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.loadByUserId("SomeUserId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testActivateForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.activate(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testActivateForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.activate("someLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testDeactivateForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.deactivate(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeactivateForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.deactivate("someLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateEmailForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updateEmail("", "someEmail", false));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateEmailForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.updateEmail("someLoginId", "someEmail", false);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdatePhoneForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updatePhone("", "someEmail", false));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdatePhoneForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.updatePhone("someLoginId", "1234567890", false);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateDisplayNameForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updateDisplayName("", "someDisplay"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateDisplayNameForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.updateDisplayName("someLoginId", "someDisplay");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdatePictureForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updatePicture("", "somePicture"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdatePictureForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.updatePicture("someLoginId", "somePicture");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateCustomAttributesForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updateCustomAttributes("", "someKey", 0));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateCustomAttributesForEmptyKey() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updateCustomAttributes("someLoginId", "", 0));
    assertNotNull(thrown);
    assertEquals("The Key argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateCustomAttributesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.updateCustomAttributes("someLoginId", "someKey", 0);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testUpdateLoginIdForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.updateLoginId("", "someId"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateLoginIdForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.updateLoginId("someLoginId", "someNewLoginId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testSetRolesForEmptyKeyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.setRoles("", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSetRolesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.setRoles("someLoginId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testAddRolesForEmptyKeyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.addRoles("", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddRolesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.addRoles("someLoginId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testRemoveRolesForEmptyKeyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.removeRoles("", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRemoveRolesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.removeRoles("someLoginId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testAddTenantForEmptyKeyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.addTenant("", "someTenantId"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddTenantForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.addTenant("someLoginId", "someTenantId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testRemoveTenantForEmptyKeyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.removeTenant("", "someTenantId"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRemoveTenantForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.removeTenant("someLoginId", "someTenantId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testSetTenantRolesForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.setTenantRoles("", "someTenantId", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSetTenantRolesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.setTenantRoles("someLoginId", "someTenantId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testAddTenantRolesForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.addTenantRoles("", "someTenantId", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddTenantRolesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.addTenantRoles("someLoginId", "someTenantId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testRemoveTenantRolesForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.removeTenantRoles("", "someTenantId", mockRoles));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRemoveTenantRolesForSuccess() {
    UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(userResponseDetails).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UserResponseDetails response = userService.removeTenantRoles("someLoginId", "someTenantId", mockRoles);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testSetPasswordForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.setPassword("", "somePassword"));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSetPasswordForEmptyPassword() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.setPassword("someLoginId", ""));
    assertNotNull(thrown);
    assertEquals("The Password argument is invalid", thrown.getMessage());
  }

  @Test
  void testSetPasswordForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      userService.setPassword("someLoginId", "somePassword");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testSetPasswordForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      userService.setPassword("someLoginId", "somePassword", true);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testExpirePasswordForEmpty() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> userService.expirePassword(""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testExpirePasswordForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      userService.expirePassword("someLoginId");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testGetProviderTokenForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.getProviderToken("", ""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetProviderTokenForEmptyProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.getProviderToken("x", ""));
    assertNotNull(thrown);
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetProviderTokenForSuccess() {
    ProviderTokenResponse mockResponse = new ProviderTokenResponse("provider", "1", "at", 1L, Arrays.asList("a", "b"));
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ProviderTokenResponse response = userService.getProviderToken("xxx", "provider");
      Assertions.assertThat(response.getProvider()).isEqualTo("provider");
      Assertions.assertThat(response.getProviderUserId()).isEqualTo("1");
      Assertions.assertThat(response.getAccessToken()).isEqualTo("at");
      Assertions.assertThat(response.getExpiration()).isEqualTo(1L);
      Assertions.assertThat(response.getScopes()).containsExactly("a", "b");
    }
  }

  @Test
  void testGenerateOtpForTestUserForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.generateOtpForTestUser("", DeliveryMethod.EMAIL));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateOtpForTestUserForSuccess() {
    OTPTestUserResponse mockResponse = new OTPTestUserResponse("12345", "someLogin");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      OTPTestUserResponse response = userService.generateOtpForTestUser("someLoginId", DeliveryMethod.EMAIL);
      Assertions.assertThat(response.getCode()).isEqualTo("12345");
    }
  }

  @Test
  void testGenerateMagicLinkForTestUserForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.generateMagicLinkForTestUser("", mockUrl, DeliveryMethod.EMAIL));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateMagicLinkForTestUserForSuccess() {
    MagicLinkTestUserResponse mockResponse = new MagicLinkTestUserResponse("link", "someLogin");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      MagicLinkTestUserResponse response =
          userService.generateMagicLinkForTestUser("someLoginId", mockUrl, DeliveryMethod.EMAIL);
      Assertions.assertThat(response.getLink()).isEqualTo("link");
    }
  }

  @Test
  void testGenerateEnchantedLinkForTestUserForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.generateEnchantedLinkForTestUser("", mockUrl));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateEnchantedLinkForTestUserForSuccess() {
    EnchantedLinkTestUserResponse mockResponse = new EnchantedLinkTestUserResponse("pref", "link", "someLoginId");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      EnchantedLinkTestUserResponse response = userService.generateEnchantedLinkForTestUser("someLoginId", mockUrl);
      Assertions.assertThat(response.getLink()).isEqualTo("link");
    }
  }

  @Test
  void testGenerateEmbeddedLinkForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.generateEmbeddedLink("", null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateEmbeddedLinkForSuccess() {
    GenerateEmbeddedLinkResponse mockResponse = new GenerateEmbeddedLinkResponse("someToken");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String response = userService.generateEmbeddedLink("someLoginId", null);
      Assertions.assertThat(response).isEqualTo("someToken");
    }
  }

  @Test
  void testSearchAllForSuccess() {
    UserResponse userResponse = mock(UserResponse.class);
    AllUsersResponseDetails allUsersResponse = new AllUsersResponseDetails(Arrays.asList(userResponse));
    UserSearchRequest userSearchRequest = UserSearchRequest.builder().limit(6).page(1).build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(allUsersResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AllUsersResponseDetails response = userService.searchAll(userSearchRequest);
      Assertions.assertThat(response.getUsers().size()).isEqualTo(1);
    }
  }

  @Test
  void testSearchAllForInvalidLimit() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.searchAll(UserSearchRequest.builder().limit(-1).build()));
    assertNotNull(thrown);
    assertEquals("The limit argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchAllForInvalidPage() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> userService.searchAll(UserSearchRequest.builder().page(-1).build()));
    assertNotNull(thrown);
    assertEquals("The page argument is invalid", thrown.getMessage());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-");
    String email = TestUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    List<String> additionalLoginIds = Arrays.asList(TestUtils.getRandomName("u-"), TestUtils.getRandomName("u-"));
    // Create
    UserResponseDetails createResponse = userService.create(loginId, UserRequest.builder().email(email)
        .verifiedEmail(true).phone(phone).verifiedPhone(true).displayName("Testing Test")
        .additionalLoginIds(additionalLoginIds).build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    Assertions.assertThat(user.getLoginIds()).contains(loginId);
    Assertions.assertThat(user.getLoginIds()).containsAll(additionalLoginIds);
    assertEquals(email, user.getEmail());
    assertEquals("+15555555555", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("invited", user.getStatus());
    // Disable
    UserResponseDetails deactivateResponse = userService.deactivate(loginId);
    user = deactivateResponse.getUser();
    assertNotNull(user);
    assertEquals("disabled", user.getStatus());
    // Enable
    UserResponseDetails activateResponse = userService.activate(loginId);
    user = activateResponse.getUser();
    assertNotNull(user);
    assertEquals("enabled", user.getStatus());
    // Update
    UserResponseDetails updateResponse = userService.update(loginId, UserRequest.builder().email(email)
        .verifiedEmail(true).phone(phone).verifiedPhone(true).displayName("Testing Test1").build());
    user = updateResponse.getUser();
    assertNotNull(user);
    assertEquals("Testing Test1", user.getName());
    // Update individual fields
    userService.updateDisplayName(loginId, "Testing Test");
    userService.updateDisplayNames(loginId, "G Test", "M Test", "F Test");
    email = TestUtils.getRandomName("test-") + "@descope.com";
    userService.updateEmail(loginId, email, true);
    userService.updatePhone(loginId, "+1-555-555-6666", true);
    String newLoginId = TestUtils.getRandomName("u-");
    userService.updateLoginId(loginId, newLoginId);
    UserResponseDetails loadResponse = userService.load(newLoginId);
    user = loadResponse.getUser();
    assertNotNull(user);
    assertEquals(email, user.getEmail());
    assertEquals("+15555556666", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("G Test", user.getGivenName());
    assertEquals("M Test", user.getMiddleName());
    assertEquals("F Test", user.getFamilyName());
    assertEquals("enabled", user.getStatus());
    loadResponse = userService.loadByUserId(createResponse.getUser().getUserId());
    assertNotNull(user);
    assertEquals(email, user.getEmail());
    assertEquals("+15555556666", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("enabled", user.getStatus());
    AllUsersResponseDetails searchResponse = userService.searchAll(null);
    boolean found = false;
    for (UserResponse u : searchResponse.getUsers()) {
      if (u.getUserId().equals(createResponse.getUser().getUserId())) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    // Delete
    userService.delete(newLoginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalTestUsers() {
    String loginId = TestUtils.getRandomName("u-");
    String email = TestUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    // Create
    UserResponseDetails createResponse = userService.createTestUser(loginId, UserRequest.builder()
        .email(email).verifiedEmail(true).phone(phone).verifiedPhone(true)
        .displayName("Testing Test").build());
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
    AllUsersResponseDetails searchResponse = userService.searchAll(UserSearchRequest.builder().withTestUser(true)
        .phones(Arrays.asList(user.getPhone())).emails(Arrays.asList(user.getEmail())).build());
    boolean found = false;
    for (UserResponse u : searchResponse.getUsers()) {
      if (u.getUserId().equals(createResponse.getUser().getUserId())) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    searchResponse = userService
        .searchAll(UserSearchRequest.builder().testUsersOnly(true).emails(Arrays.asList(user.getEmail())).build());
    found = false;
    for (UserResponse u : searchResponse.getUsers()) {
      if (u.getUserId().equals(createResponse.getUser().getUserId())) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    // Delete
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUserWithTenantAndRole() {
    String tenantName = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(tenantName, Arrays.asList(tenantName + ".com"));
    assertThat(tenantId).isNotBlank();
    String roleName = TestUtils.getRandomName("r-").substring(0, 20);
    roleService.create(roleName, "", null);
    String loginId = TestUtils.getRandomName("u-");
    String email = TestUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    // Create
    UserResponseDetails createResponse = userService.create(loginId,
        UserRequest.builder().email(email).verifiedEmail(true).phone(phone).verifiedPhone(true)
            .displayName("Testing Test")
            .userTenants(
              Arrays.asList(AssociatedTenant.builder().tenantId(tenantId).roleNames(Arrays.asList(roleName)).build()))
            .build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    assertThat(user.getLoginIds()).contains(loginId);
    assertEquals(email, user.getEmail());
    assertEquals("+15555555555", user.getPhone());
    assertEquals(true, user.getVerifiedEmail());
    assertEquals(true, user.getVerifiedPhone());
    assertEquals("Testing Test", user.getName());
    assertEquals("invited", user.getStatus());
    assertThat(user.getUserTenants()).containsExactly(
        AssociatedTenant.builder().tenantId(tenantId).tenantName(tenantName).roleNames(
          Arrays.asList(roleName)).build());
    UserResponseDetails updateResponse = userService.update(loginId,
        UserRequest.builder().roleNames(Arrays.asList(roleName)).email(email).verifiedEmail(true)
            .phone(phone).verifiedPhone(true).displayName("Testing Test").build());
    user = updateResponse.getUser();
    assertNotNull(user);
    assertThat(user.getRoleNames()).containsExactly(roleName);
    // Delete
    userService.delete(loginId);
    tenantService.delete(tenantId);
    roleService.delete(roleName);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalGenerateEmbeddedLink() {
    String loginId = TestUtils.getRandomName("u-");
    String email = TestUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    // Create
    UserResponseDetails createResponse = userService.create(loginId, UserRequest.builder().email(email)
        .verifiedEmail(true).phone(phone).verifiedPhone(true).displayName("Testing Test").build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    Assertions.assertThat(user.getLoginIds()).contains(loginId);
    String token = userService.generateEmbeddedLink(loginId, null);
    AuthenticationInfo authInfo = magicLinkService.verify(token);
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    token = userService.generateEmbeddedLink(loginId, mapOf("kuku", "kiki"));
    final long now = System.currentTimeMillis();
    authInfo = magicLinkService.verify(token);
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    Map<String, Object> claims = authInfo.getToken().getClaims();
    // temporary
    @SuppressWarnings("unchecked")
    Map<String, Object> nsecClaims = Map.class.cast(claims.get("nsec"));
    assertEquals("kiki", nsecClaims == null ? claims.get("kuku") : nsecClaims.get("kuku"));

    // sleep till we are more than a sec than 'now'
    while ((System.currentTimeMillis() - 1000) < now) {
      try {
        Thread.sleep(100);
      } catch (Throwable thr) {
        fail("shouldn't happen");
      }
    }

    List<UserHistoryResponse> history = userService.history(Arrays.asList(user.getUserId()));
    assertThat(history).isNotEmpty();
    history = authenticationService.history(authInfo.getRefreshToken().getJwt());
    assertThat(history).isNotEmpty();
    // now logout and see that we logged out successfully
    userService.logoutUser(loginId);
    boolean gotExc = false;
    try {
      authenticationService.refreshSessionWithToken(authInfo.getToken().getJwt());
      fail("Refresh should fail after logout");
    } catch (DescopeException de) {
      gotExc = true;
    }
    assertEquals(true, gotExc);
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalGenerateEmbeddedLinkWithPhoneAsID() {
    String randomSaffix = String.valueOf(new Random().nextInt(1000));
    randomSaffix = new String(new char[4 - randomSaffix.length()]).replace("\0", "0") + randomSaffix;
    String phone = "+1-555-555-" + randomSaffix;
    String cleanPhone = "+1555555" + randomSaffix;
    // Create
    UserResponseDetails createResponse = userService.create(phone, UserRequest.builder().phone(phone)
        .verifiedPhone(true).displayName("Testing Test").build());
    UserResponse user = createResponse.getUser();
    assertNotNull(user);
    Assertions.assertThat(user.getLoginIds()).contains(cleanPhone);
    String token = userService.generateEmbeddedLink(phone, null);
    AuthenticationInfo authInfo = magicLinkService.verify(token);
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    UserResponseDetails userResp = userService.load(cleanPhone);
    assertNotNull(userResp.getUser());
    Assertions.assertThat(userResp.getUser().getLoginIds()).contains(cleanPhone);
    userService.delete(phone);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalBatch() throws Exception {
    String loginId = TestUtils.getRandomName("u-");
    String email = TestUtils.getRandomName("test-") + "@descope.com";
    String phone = "+1-555-555-5555";
    String name = "Kuku McKiki";
    // Create a password hash to test
    String password = "This is a test";
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    byte[] hash = factory.generateSecret(spec).getEncoded();

    UsersBatchResponse res = userService.createBatch(Arrays.asList(BatchUserRequest.builder()
        .loginId(loginId)
        .email(email)
        .verifiedEmail(true)
        .phone(phone)
        .verifiedPhone(true)
        .displayName(name)
        .hashedPassword(BatchUserPasswordHashed.builder()
            .algorithm(BatchUserPasswordAlgorithm.BATCH_USER_PASSWORD_ALGORITHM_PBKDF2SHA1)
            .hash(hash)
            .salt(salt)
            .iterations(65536)
            .build())
        .build()));
    assertNotNull(res);
    assertNotNull(res.getCreatedUsers());
    assertEquals(1, res.getCreatedUsers().size());
    assertTrue(res.getFailedUsers() == null || res.getFailedUsers().isEmpty());
    AuthenticationInfo authInfo = passwordService.signIn(loginId, password);
    assertNotNull(authInfo);
    assertNotNull(authInfo.getUser());
    assertEquals(email, authInfo.getUser().getEmail());
    assertEquals(name, authInfo.getUser().getName());
    userService.delete(loginId);
  }
}
