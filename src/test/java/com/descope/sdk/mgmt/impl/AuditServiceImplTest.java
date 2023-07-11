package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.AuditService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AuditServiceImplTest {
  private AuditService auditService;
  // Used to generate a few audit rows
  private AccessKeyService accessKeyService;

  @BeforeEach
  void setUp() {
    var authParams = TestMgmtUtils.getManagementParams();
    var client = TestMgmtUtils.getClient();
    var mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
    this.auditService = mgmtServices.getAuditService();
    this.accessKeyService = mgmtServices.getAccessKeyService();
  }

  @Test
  void testSearchForSuccess() {
    var auditRecord = mock(AuditServiceImpl.ActualAuditRecord.class);
    var auditResponse = new AuditServiceImpl.ActualAuditSearchResponse(List.of(auditRecord));
    var now = Instant.now();
    auditRecord.occurred = String.valueOf(now.toEpochMilli());
    auditRecord.externalIds = List.of("id1", "id2");
    var auditSearchRequest = new AuditSearchRequest();
    var apiProxy = mock(ApiProxy.class);
    doReturn(auditResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = auditService.search(auditSearchRequest);
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

  @Test
  @SneakyThrows
  void testFunctionalFullCycle() {
    var createResult = accessKeyService.create(
        TestMgmtUtils.getRandomName("ak-"), 0, null, null);
    accessKeyService.delete(createResult.getKey().getId());
    // Wait for the audit
    Thread.sleep(60000);
    var searchRes = auditService.search(AuditSearchRequest.builder().noTenants(true).build());
    Assertions.assertThat(searchRes.getAudits()).isNotEmpty();
  }
}
