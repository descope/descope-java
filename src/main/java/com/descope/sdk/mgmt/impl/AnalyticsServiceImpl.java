package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ANALYTICS_SEARCH;

import com.descope.exception.DescopeException;
import com.descope.model.analytics.AnalyticRecord;
import com.descope.model.analytics.AnalyticsSearchRequest;
import com.descope.model.analytics.AnalyticsSearchResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AnalyticsService;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class AnalyticsServiceImpl extends ManagementsBase implements AnalyticsService {

  AnalyticsServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AnalyticsSearchResponse search(AnalyticsSearchRequest request) throws DescopeException {
    if (request == null) {
      request = new AnalyticsSearchRequest();
    }
    ApiProxy apiProxy = getApiProxy();
    ActualAnalyticsSearchRequest actualReq = new ActualAnalyticsSearchRequest(
        toMillisString(request.getFrom()),
        toMillisString(request.getTo()),
        request.getActions(),
        request.getExcludedActions(),
        request.getDevices(),
        request.getMethods(),
        request.getGeos(),
        request.getTenants(),
        request.getGroupByAction(),
        request.getGroupByDevice(),
        request.getGroupByMethod(),
        request.getGroupByGeo(),
        request.getGroupByTenant(),
        request.getGroupByReferrer(),
        request.getGroupByCreated());
    ActualAnalyticsSearchResponse resp = (ActualAnalyticsSearchResponse) apiProxy.post(
        getUri(MANAGEMENT_ANALYTICS_SEARCH), actualReq, ActualAnalyticsSearchResponse.class);
    List<AnalyticRecord> res = new ArrayList<>();
    if (resp != null && resp.getAnalytics() != null) {
      for (ActualAnalyticRecord r : resp.getAnalytics()) {
        res.add(new AnalyticRecord(
            r.projectId,
            r.action,
            r.created != null ? Instant.ofEpochMilli(Long.parseLong(r.created)) : null,
            r.device,
            r.method,
            r.geo,
            r.tenant,
            r.referrer,
            r.cnt != null ? Long.parseLong(r.cnt) : 0));
      }
    }
    return new AnalyticsSearchResponse(res);
  }

  // int64 fields travel as strings
  private static String toMillisString(Instant t) {
    return t != null ? String.valueOf(t.toEpochMilli()) : null;
  }

  @Data
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  static class ActualAnalyticsSearchRequest {
    String from;
    String to;
    List<String> actions;
    List<String> excludedActions;
    List<String> devices;
    List<String> methods;
    List<String> geos;
    List<String> tenants;
    Boolean groupByAction;
    Boolean groupByDevice;
    Boolean groupByMethod;
    Boolean groupByGeo;
    Boolean groupByTenant;
    Boolean groupByReferrer;
    String groupByCreated;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class ActualAnalyticRecord {
    String projectId;
    String action;
    String created;
    String device;
    String method;
    String geo;
    String tenant;
    String referrer;
    String cnt;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class ActualAnalyticsSearchResponse {
    List<ActualAnalyticRecord> analytics;
  }
}
