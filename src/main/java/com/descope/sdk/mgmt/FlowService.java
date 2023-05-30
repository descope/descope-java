package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;

public interface FlowService {
  FlowResponse exportFlow(String flowID) throws DescopeException;

  FlowResponse ImportFlow(String flowID, Flow flow, Screen screen) throws DescopeException;

  Theme ExportTheme() throws DescopeException;

  Theme ImportTheme(Theme theme) throws DescopeException;
}
