package com.descope.sdk.mgmt.impl;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.model.mgmt.ManagementParams;
import com.descope.sdk.mgmt.FlowService;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

import static com.descope.literals.Routes.ManagementEndPoints.FLOW_EXPORT;
import static com.descope.literals.Routes.ManagementEndPoints.FLOW_IMPORT;
import static com.descope.literals.Routes.ManagementEndPoints.THEME_EXPORT;
import static com.descope.literals.Routes.ManagementEndPoints.THEME_IMPORT;

public class FlowServiceImpl extends ManagementsBase implements FlowService {

  FlowServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public FlowResponse exportFlow(String flowID) throws DescopeException {
    if (StringUtils.isBlank(flowID)) {
      throw ServerCommonException.invalidArgument("FlowID");
    }
    Map<String, String> request = Map.of("flowId", flowID);
    var apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_EXPORT), request, FlowResponse.class);
  }

  @Override
  public FlowResponse ImportFlow(String flowID, Flow flow, Screen screen) throws DescopeException {
    if (StringUtils.isBlank(flowID)) {
      throw ServerCommonException.invalidArgument("FlowID");
    }
    Map<String, Object> request = Map.of("flowId", flowID, "flow", flow, "screens", screen);
    var apiProxy = getApiProxy();
    return apiProxy.post(getUri(FLOW_IMPORT), request, FlowResponse.class);
  }

  @Override
  public Theme ExportTheme() throws DescopeException {
    var apiProxy = getApiProxy();
    return apiProxy.post(getUri(THEME_EXPORT), null, Theme.class);
  }

  @Override
  public Theme ImportTheme(Theme theme) throws DescopeException {
    if (Objects.isNull(theme)) {
      throw ServerCommonException.invalidArgument("Theme");
    }
    Map<String, Object> request = Map.of("theme", theme);
    var apiProxy = getApiProxy();
    return apiProxy.post(getUri(THEME_IMPORT), request, Theme.class);
  }

}
