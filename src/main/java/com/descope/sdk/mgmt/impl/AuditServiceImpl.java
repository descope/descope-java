package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUDIT_SEARCH_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.audit.AuditRecord;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.sdk.mgmt.AuditService;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

class AuditServiceImpl extends ManagementsBase implements AuditService {

  AuditServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<AuditRecord> search(AuditSearchRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = new AuditSearchRequest();
    }
    var now = Instant.now();
    var oldest = now.minus(Duration.ofDays(30));
    if (!Objects.isNull(request.getFrom()) && request.getFrom().isBefore(oldest)) {
      throw ServerCommonException.invalidArgument("from");
    }
    if (!Objects.isNull(request.getTo()) && now.isBefore(request.getTo())) {
      throw ServerCommonException.invalidArgument("to");
    }

    URI composeSearchUri = composeSearchUri();
    var apiProxy = getApiProxy();
    var actualReq = new ActualAuditSearchRequest(request.getUserIds(), request.getActions(), request.getExcludedActions(),
      request.getDevices(), request.getMethods(), request.getGeos(), request.getRemoteAddresses(), request.getLoginIds(), request.getTenants(),
      request.isNoTenants(), request.getText(), request.getFrom().toEpochMilli(), request.getTo().toEpochMilli());
    var resp = (List<ActualAuditRecord>) apiProxy.post(composeSearchUri, actualReq, List.class);
    var res = new ArrayList<AuditRecord>();
    for (var a : resp) {
      res.add(new AuditRecord(a.projectId, a.userId, a.action, Instant.ofEpochMilli(Long.parseLong(a.occurred)), a.device,
        a.method, a.geo, a.remoteAddress, a.externalIds, a.tenants, a.data));
    }
    return res;
  }

  private URI composeSearchUri() {
    return getUri(MANAGEMENT_AUDIT_SEARCH_LINK);
  }

  @Data
  @AllArgsConstructor
  private static class ActualAuditSearchRequest {
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
  @AllArgsConstructor
  private static class ActualAuditRecord {
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
}
