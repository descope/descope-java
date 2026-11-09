package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ANALYTICS_SEARCH;

import com.descope.exception.DescopeException;
import com.descope.model.analytics.AnalyticRecord;
import com.descope.model.analytics.AnalyticsSearchRequest;
import com.descope.model.analytics.AnalyticsSearchResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AnalyticsService;
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
    ActualAnalyticsSearchRequest actualReq = new ActualAnalyticsSearchRequest(
        toEpochMilliString(request.getFrom()),
        toEpochMilliString(request.getTo()),
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
    ApiProxy apiProxy = getApiProxy();
    ActualAnalyticsSearchResponse resp = (ActualAnalyticsSearchResponse) apiProxy.post(
        getUri(MANAGEMENT_ANALYTICS_SEARCH), actualReq, ActualAnalyticsSearchResponse.class);
    List<AnalyticRecord> res = new ArrayList<>();
    if (resp != null && resp.getAnalytics() != null) {
      for (ActualAnalyticRecord rec : resp.getAnalytics()) {
        if (rec == null) {
          continue;
        }
        res.add(new AnalyticRecord(
            rec.projectId,
            rec.action,
            rec.created != null ? Instant.ofEpochMilli(Long.parseLong(rec.created)) : null,
            rec.device,
            rec.method,
            rec.geo,
            rec.tenant,
            rec.referrer,
            rec.cnt != null ? Long.parseLong(rec.cnt) : null));
      }
    }
    return new AnalyticsSearchResponse(res);
  }

  // int64 values travel as quoted strings
  private static String toEpochMilliString(Instant instant) {
    return instant != null ? String.valueOf(instant.toEpochMilli()) : null;
  }

  @Data
  @AllArgsConstructor
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
