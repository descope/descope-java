package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.FLOW_EXPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_IMPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_LIST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.THEME_EXPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.THEME_IMPORT_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.model.flow.ThemeResponse;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.FlowService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class FlowServiceImpl extends ManagementsBase implements FlowService {

  FlowServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public FlowsResponse listFlows() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_LIST_LINK), null, FlowsResponse.class);
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
}
