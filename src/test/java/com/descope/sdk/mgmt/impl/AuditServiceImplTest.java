package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.enums.AuditType;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.audit.AuditCreateRequest;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.audit.AuditSearchResponse;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AuditService;
import com.descope.sdk.mgmt.impl.AuditServiceImpl.ActualAuditRecord;
import com.descope.sdk.mgmt.impl.AuditServiceImpl.ActualAuditSearchResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class AuditServiceImplTest {
  private AuditService auditService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.auditService = mgmtServices.getAuditService();
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
      assertThat(response.getAudits().size()).isEqualTo(1);
      assertThat(response.getAudits().get(0).getOccurred().toEpochMilli())
          .isEqualTo(now.toEpochMilli());
      assertThat(response.getAudits().get(0).getLoginIds().size()).isEqualTo(2);
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
    String actor = TestUtils.getRandomName("act-");
    auditService.createEvent(
      new AuditCreateRequest("123456789012345678901234567890", "kiki", AuditType.INFO, actor, "t", mapOf("a", "b")));
    Thread.sleep(60000);
    AuditSearchResponse searchRes = auditService.search(
        AuditSearchRequest.builder()
          .actorIds(Arrays.asList(actor))
          .tenants(Arrays.asList("t"))
          .build());
    assertThat(searchRes.getAudits()).isNotEmpty();
    assertThat(searchRes.getAudits().get(0).getAction()).isEqualTo("kiki");
    assertThat(searchRes.getAudits().get(0).getActorId()).isEqualTo(actor);
    assertThat(searchRes.getAudits().get(0).getUserId()).isEqualTo("123456789012345678901234567890");
    assertThat(searchRes.getAudits().get(0).getType()).isEqualTo(AuditType.INFO);
    assertThat(searchRes.getAudits().get(0).getTenants()).isNotEmpty();
  }
}
