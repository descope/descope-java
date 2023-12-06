package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.audit.AuditSearchResponse;
import com.descope.model.client.Client;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.AuditService;
import com.descope.sdk.mgmt.impl.AuditServiceImpl.ActualAuditRecord;
import com.descope.sdk.mgmt.impl.AuditServiceImpl.ActualAuditSearchResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class AuditServiceImplTest {
  private AuditService auditService;
  // Used to generate a few audit rows
  private AccessKeyService accessKeyService;

  @BeforeEach
  void setUp() {
    ManagementParams authParams = TestUtils.getManagementParams();
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
    this.auditService = mgmtServices.getAuditService();
    this.accessKeyService = mgmtServices.getAccessKeyService();
  }

  @Test
  void testSearchForSuccess() {
    ActualAuditRecord auditRecord = mock(AuditServiceImpl.ActualAuditRecord.class);
    ActualAuditSearchResponse auditResponse =
        new AuditServiceImpl.ActualAuditSearchResponse(Arrays.asList(auditRecord));
    Instant now = Instant.now();
    auditRecord.occurred = String.valueOf(now.toEpochMilli());
    auditRecord.externalIds = Arrays.asList("id1", "id2");
    AuditSearchRequest auditSearchRequest = new AuditSearchRequest();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(auditResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AuditSearchResponse response = auditService.search(auditSearchRequest);
      Assertions.assertThat(response.getAudits().size()).isEqualTo(1);
      Assertions.assertThat(response.getAudits().get(0).getOccurred().toEpochMilli())
          .isEqualTo(now.toEpochMilli());
      Assertions.assertThat(response.getAudits().get(0).getLoginIds().size()).isEqualTo(2);
    }
  }

  @Test
  void testSearchAllForInvalidFrom() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                auditService.search(
                    AuditSearchRequest.builder()
                        .from(Instant.now().minus(Duration.ofDays(31)))
                        .build()));
    assertNotNull(thrown);
    assertEquals("The from argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchAllForInvalidTo() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                auditService.search(
                    AuditSearchRequest.builder()
                        .to(Instant.now().plus(Duration.ofDays(1)))
                        .build()));
    assertNotNull(thrown);
    assertEquals("The to argument is invalid", thrown.getMessage());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() throws Exception {
    AccessKeyResponse createResult = accessKeyService.create(
        TestUtils.getRandomName("ak-"), 0, null, null);
    accessKeyService.delete(createResult.getKey().getId());
    // Wait for the audit
    Thread.sleep(10000);
    AuditSearchResponse searchRes = auditService.search(AuditSearchRequest.builder().noTenants(true).build());
    Assertions.assertThat(searchRes.getAudits()).isNotEmpty();
  }
}
