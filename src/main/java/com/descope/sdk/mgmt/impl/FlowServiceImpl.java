package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.FLOW_ASYNC_RESULT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_EXPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_IMPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_LIST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_RUN_ASYNC_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_RUN_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.THEME_EXPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.THEME_IMPORT_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.ManagementFlowOptions;
import com.descope.model.flow.RunManagementFlowAsyncResponse;
import com.descope.model.flow.RunManagementFlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.model.flow.ThemeResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.FlowService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

class FlowServiceImpl extends ManagementsBase implements FlowService {

  FlowServiceImpl(Client client) {
    super(client);
  }

  @Override
  public FlowsResponse listFlows() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_LIST_LINK), null, FlowsResponse.class);
  }

  @Override
  public FlowsResponse listFlows(List<String> ids) throws DescopeException {
    Map<String, Object> request = new HashMap<>();
    if (ids != null) {
      request.put("ids", ids);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_LIST_LINK), request, FlowsResponse.class);
  }

  @Override
  public void deleteFlows(List<String> flowIds) throws DescopeException {
    if (CollectionUtils.isEmpty(flowIds)) {
      throw ServerCommonException.invalidArgument("flowIds");
    }
    Map<String, Object> request = mapOf("ids", flowIds);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(FLOW_DELETE_LINK), request, Void.class);
  }

  @Override
  public FlowResponse exportFlow(String flowID) throws DescopeException {
    if (StringUtils.isBlank(flowID)) {
      throw ServerCommonException.invalidArgument("FlowID");
    }
    Map<String, String> request = mapOf("flowId", flowID);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_EXPORT_LINK), request, FlowResponse.class);
  }

  @Override
  public FlowResponse importFlow(String flowID, Flow flow, List<Screen> screens)
      throws DescopeException {
    if (StringUtils.isBlank(flowID)) {
      throw ServerCommonException.invalidArgument("FlowID");
    }
    Map<String, Object> request = mapOf("flowId", flowID, "flow", flow, "screens", screens);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_IMPORT_LINK), request, FlowResponse.class);
  }

  @Override
  public Theme exportTheme() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(THEME_EXPORT_LINK), null, ThemeResponse.class).getTheme();
  }

  @Override
  public Theme importTheme(Theme theme) throws DescopeException {
    if (theme == null) {
      throw ServerCommonException.invalidArgument("Theme");
    }
    Map<String, Object> request = mapOf("theme", theme);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(THEME_IMPORT_LINK), request, ThemeResponse.class).getTheme();
  }

  @Override
  public Map<String, Object> runManagementFlow(String flowId, ManagementFlowOptions options)
      throws DescopeException {
    RunManagementFlowResponse response = getApiProxy().post(
        getUri(FLOW_RUN_LINK), runManagementFlowRequest(flowId, options), RunManagementFlowResponse.class);
    return response == null ? null : response.getOutput();
  }

  @Override
  public String runManagementFlowAsync(String flowId, ManagementFlowOptions options)
      throws DescopeException {
    RunManagementFlowAsyncResponse response = getApiProxy().post(
        getUri(FLOW_RUN_ASYNC_LINK), runManagementFlowRequest(flowId, options),
        RunManagementFlowAsyncResponse.class);
    return response == null ? null : response.getExecutionId();
  }

  @Override
  public Map<String, Object> getManagementFlowAsyncResult(String executionId) throws DescopeException {
    if (StringUtils.isBlank(executionId)) {
      throw ServerCommonException.invalidArgument("ExecutionID");
    }
    Map<String, Object> request = mapOf("executionId", executionId);
    RunManagementFlowResponse response = getApiProxy().post(
        getUri(FLOW_ASYNC_RESULT_LINK), request, RunManagementFlowResponse.class);
    return response == null ? null : response.getOutput();
  }

  private static Map<String, Object> runManagementFlowRequest(String flowId, ManagementFlowOptions options) {
    if (StringUtils.isBlank(flowId)) {
      throw ServerCommonException.invalidArgument("FlowID");
    }
    Map<String, Object> request = mapOf("flowId", flowId);
    if (options != null) {
      request.put("options", options);
    }
    return request;
  }
}
