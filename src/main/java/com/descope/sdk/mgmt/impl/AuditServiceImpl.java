package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUDIT_SEARCH_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.audit.AuditRecord;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.audit.AuditSearchResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AuditService;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class AuditServiceImpl extends ManagementsBase implements AuditService {

  AuditServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AuditSearchResponse search(AuditSearchRequest request) throws DescopeException {
    if (request == null) {
      request = new AuditSearchRequest();
    }
    Instant now = Instant.now();
    Instant oldest = now.minus(Duration.ofDays(30));
    if (request.getFrom() != null && request.getFrom().isBefore(oldest)) {
      throw ServerCommonException.invalidArgument("from");
    }
    if (request.getTo() != null && now.isBefore(request.getTo())) {
      throw ServerCommonException.invalidArgument("to");
    }

    URI composeSearchUri = composeSearchUri();
    ApiProxy apiProxy = getApiProxy();
    ActualAuditSearchRequest actualReq = new ActualAuditSearchRequest(
        request.getUserIds(),
        request.getActions(),
        request.getExcludedActions(),
        request.getDevices(),
        request.getMethods(),
        request.getGeos(),
        request.getRemoteAddresses(),
        request.getLoginIds(),
        request.getTenants(),
        request.isNoTenants(),
        request.getText(),
        request.getFrom() != null ? request.getFrom().toEpochMilli() : 0,
        request.getTo() != null ? request.getTo().toEpochMilli() : 0);
    ActualAuditSearchResponse resp =
        (ActualAuditSearchResponse) apiProxy.post(composeSearchUri, actualReq, ActualAuditSearchResponse.class);
    List<AuditRecord> res = new ArrayList<AuditRecord>();
    for (ActualAuditRecord auditRecord : resp.getAudits()) {
      res.add(
          new AuditRecord(
              auditRecord.projectId,
              auditRecord.userId,
              auditRecord.action,
              Instant.ofEpochMilli(
                  auditRecord.occurred != null ? Long.parseLong(auditRecord.occurred) : 0),
              auditRecord.device,
              auditRecord.method,
              auditRecord.geo,
              auditRecord.remoteAddress,
              auditRecord.externalIds,
              auditRecord.tenants,
              auditRecord.data));
    }
    return new AuditSearchResponse(res);
  }

  private URI composeSearchUri() {
    return getUri(MANAGEMENT_AUDIT_SEARCH_LINK);
  }

  @Data
  @AllArgsConstructor
  static class ActualAuditSearchRequest {
    List<String> userIds;
    List<String> actions;
    List<String> excludedActions;
    List<String> devices;
    List<String> methods;
    List<String> geos;
    List<String> remoteAddresses;
    List<String> externalIds;
    List<String> tenants;
    boolean noTenants;
    String text;
    long from;
    long to;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class ActualAuditRecord {
    String projectId;
    String userId;
    String action;
    String occurred;
    String device;
    String method;
    String geo;
    String remoteAddress;
    List<String> externalIds;
    List<String> tenants;
    Map<String, Object> data;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class ActualAuditSearchResponse {
    List<ActualAuditRecord> audits;
  }
}
