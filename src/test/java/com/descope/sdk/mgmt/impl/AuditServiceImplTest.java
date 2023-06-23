package com.descope.sdk.mgmt.impl;

import com.descope.exception.ServerCommonException;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AuditService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class AuditServiceImplTest {

  public static final String MOCK_PROJECT_ID = "someProjectId";
  private AuditService auditService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.auditService = ManagementServiceBuilder.buildServices(client, authParams).getAuditService();
  }

  @Test
  void testSearchForSuccess() {
    var auditResponse = mock(AuditServiceImpl.ActualAuditRecord.class);
    var now = Instant.now();
    auditResponse.occurred = String.valueOf(now.toEpochMilli());
    auditResponse.externalIds = List.of("id1", "id2");
    var auditSearchRequest = new AuditSearchRequest();
    var apiProxy = mock(ApiProxy.class);
    doReturn(List.of(auditResponse)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = auditService.search(auditSearchRequest);
      Assertions.assertThat(response.size()).isEqualTo(1);
      Assertions.assertThat(response.get(0).getOccurred().toEpochMilli()).isEqualTo(now.toEpochMilli());
      Assertions.assertThat(response.get(0).getLoginIds().size()).isEqualTo(2);
    }
  }

  @Test
  void testSearchAllForInvalidFrom() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> auditService.search(
      AuditSearchRequest.builder().from(Instant.now().minus(Duration.ofDays(31))).build()));
    assertNotNull(thrown);
    assertEquals("The from argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchAllForInvalidTo() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> auditService.search(
      AuditSearchRequest.builder().to(Instant.now().plus(Duration.ofDays(1))).build()));
    assertNotNull(thrown);
    assertEquals("The to argument is invalid", thrown.getMessage());
  }
}

