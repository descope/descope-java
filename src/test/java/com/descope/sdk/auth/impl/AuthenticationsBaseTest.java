package com.descope.sdk.auth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class AuthenticationsBaseTest {

  @Test
  void testGetApiProxyWithAuthManagementKey() {
    String projectId = "P123456789012345678901234567";
    String authManagementKey = "auth-mgmt-key-123";
    
    Client client = Client.builder()
        .projectId(projectId)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("test").build())
        .build();
    
    ApiProxy mockProxy = mock(ApiProxy.class);
    ArgumentCaptor<Supplier<String>> authHeaderCaptor = ArgumentCaptor.forClass(Supplier.class);
    
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(authHeaderCaptor.capture(), any(Client.class)))
          .thenReturn(mockProxy);
      
      OTPServiceImpl otpService = new OTPServiceImpl(client);
      otpService.getApiProxy();
      
      String authHeader = authHeaderCaptor.getValue().get();
      assertThat(authHeader).isEqualTo("Bearer " + projectId + ":" + authManagementKey);
    }
  }

  @Test
  void testGetApiProxyWithoutAuthManagementKey() {
    String projectId = "P123456789012345678901234567";
    
    Client client = Client.builder()
        .projectId(projectId)
        .sdkInfo(SdkInfo.builder().name("test").build())
        .build();
    
    ApiProxy mockProxy = mock(ApiProxy.class);
    ArgumentCaptor<Supplier<String>> authHeaderCaptor = ArgumentCaptor.forClass(Supplier.class);
    
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(authHeaderCaptor.capture(), any(Client.class)))
          .thenReturn(mockProxy);
      
      OTPServiceImpl otpService = new OTPServiceImpl(client);
      otpService.getApiProxy();
      
      String authHeader = authHeaderCaptor.getValue().get();
      assertThat(authHeader).isEqualTo("Bearer " + projectId);
    }
  }

  @Test
  void testGetApiProxyWithRefreshTokenAndAuthManagementKey() {
    String projectId = "P123456789012345678901234567";
    String authManagementKey = "auth-mgmt-key-123";
    String refreshToken = "refresh-token-456";
    
    Client client = Client.builder()
        .projectId(projectId)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("test").build())
        .build();
    
    ApiProxy mockProxy = mock(ApiProxy.class);
    ArgumentCaptor<Supplier<String>> authHeaderCaptor = ArgumentCaptor.forClass(Supplier.class);
    
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(authHeaderCaptor.capture(), any(Client.class)))
          .thenReturn(mockProxy);
      
      OTPServiceImpl otpService = new OTPServiceImpl(client);
      otpService.getApiProxy(refreshToken);
      
      String authHeader = authHeaderCaptor.getValue().get();
      assertThat(authHeader).isEqualTo("Bearer " + projectId + ":" + refreshToken + ":" + authManagementKey);
    }
  }

  @Test
  void testGetApiProxyWithRefreshTokenWithoutAuthManagementKey() {
    String projectId = "P123456789012345678901234567";
    String refreshToken = "refresh-token-456";
    
    Client client = Client.builder()
        .projectId(projectId)
        .sdkInfo(SdkInfo.builder().name("test").build())
        .build();
    
    ApiProxy mockProxy = mock(ApiProxy.class);
    ArgumentCaptor<Supplier<String>> authHeaderCaptor = ArgumentCaptor.forClass(Supplier.class);
    
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(authHeaderCaptor.capture(), any(Client.class)))
          .thenReturn(mockProxy);
      
      OTPServiceImpl otpService = new OTPServiceImpl(client);
      otpService.getApiProxy(refreshToken);
      
      String authHeader = authHeaderCaptor.getValue().get();
      assertThat(authHeader).isEqualTo("Bearer " + projectId + ":" + refreshToken);
    }
  }

  @Test
  void testGetApiProxyWithEmptyAuthManagementKey() {
    String projectId = "P123456789012345678901234567";
    
    Client client = Client.builder()
        .projectId(projectId)
        .authManagementKey("")
        .sdkInfo(SdkInfo.builder().name("test").build())
        .build();
    
    ApiProxy mockProxy = mock(ApiProxy.class);
    ArgumentCaptor<Supplier<String>> authHeaderCaptor = ArgumentCaptor.forClass(Supplier.class);
    
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(authHeaderCaptor.capture(), any(Client.class)))
          .thenReturn(mockProxy);
      
      OTPServiceImpl otpService = new OTPServiceImpl(client);
      otpService.getApiProxy();
      
      String authHeader = authHeaderCaptor.getValue().get();
      assertThat(authHeader).isEqualTo("Bearer " + projectId);
    }
  }
}
