package com.descope.sdk.impl;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseDetails;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// TODO - need to do functional testing
class AccessKeyServiceImplTest {
    private final List<String> mockRoles = List.of("Test");
    private final AssociatedTenant associatedTenant = new AssociatedTenant("test", mockRoles);
    private final List<AssociatedTenant> mockKeyTenants = List.of(associatedTenant);
    private final AccessKeyResponseDetails mockResponse =
            AccessKeyResponseDetails.builder()
                    .keyTenants(mockKeyTenants)
                    .name("name")
                    .roleNames(mockRoles)
                    .id("id")
                    .createdBy("TestUSer")
                    .createdTime(123456789023L)
                    .build();
    private final AccessKeyResponse mockAccessResponse = new AccessKeyResponse(mockResponse);
    private AccessKeyService accessKeyService;

    @BeforeEach
    void setUp() {
        var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
        var client = Client.builder().uri("https://api.descope.com/v1").build();
        this.accessKeyService =
                ManagementServiceBuilder.buildServices(client, authParams).getAccessKeyService();
    }

    @Test
    void testCreateForEmptyName() {
        ServerCommonException thrown =
                assertThrows(
                        ServerCommonException.class,
                        () -> accessKeyService.create("", 10, mockRoles, mockKeyTenants));
        assertNotNull(thrown);
        assertEquals("The Name argument is invalid", thrown.getMessage());
    }

    @Test
    void testCreateForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            JsonNode response = accessKeyService.create("Test", 10, mockRoles, mockKeyTenants);
            assertNotNull(response);
        }
    }

    @Test
    void testLoadForEmptyId() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class, () -> accessKeyService.load(""));
        assertNotNull(thrown);
        assertEquals("The Id argument is invalid", thrown.getMessage());
    }

    @Test
    void testLoadForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(mockAccessResponse).when(apiProxy).get(any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            AccessKeyResponse response = accessKeyService.load("Id");
            Assertions.assertThat(response.getKey().getId()).isNotBlank();
        }
    }

    @Test
    void testUpdateForEmptyName() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class, () -> accessKeyService.update("Id", ""));
        assertNotNull(thrown);
        assertEquals("The Name argument is invalid", thrown.getMessage());
    }

    @Test
    void testUpdateForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            AccessKeyResponse response = accessKeyService.update("Test", "name");
            Assertions.assertThat(response.getKey().getId()).isNotBlank();
        }
    }

    @Test
    void testUpdateForEmptyId() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class, () -> accessKeyService.update("", "Krishna"));
        assertNotNull(thrown);
        assertEquals("The Id argument is invalid", thrown.getMessage());
    }

    @Test
    void testDeactivateForEmptyId() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class, () -> accessKeyService.deactivate(""));
        assertNotNull(thrown);
        assertEquals("The Id argument is invalid", thrown.getMessage());
    }

    @Test
    void testDeactivateForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            AccessKeyResponse response = accessKeyService.deactivate("Test");
            Assertions.assertThat(response.getKey().getId()).isNotBlank();
        }
    }

    @Test
    void testActivateForEmptyId() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class, () -> accessKeyService.activate(""));
        assertNotNull(thrown);
        assertEquals("The Id argument is invalid", thrown.getMessage());
    }

    @Test
    void testActivateForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            AccessKeyResponse response = accessKeyService.activate("Test");
            Assertions.assertThat(response.getKey().getId()).isNotBlank();
        }
    }

    @Test
    void testDeleteForEmptyId() {
        ServerCommonException thrown =
                assertThrows(ServerCommonException.class, () -> accessKeyService.delete(""));
        assertNotNull(thrown);
        assertEquals("The Id argument is invalid", thrown.getMessage());
    }

    @Test
    void testDeleteForSuccess() {
        var apiProxy = mock(ApiProxy.class);
        doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
        try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
            accessKeyService.delete("Test");
            verify(apiProxy, times(1)).post(any(), any(), any());
        }
    }
}
