package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import java.util.List;

public interface FlowService {
  // TODO: Add List Flows

  FlowResponse exportFlow(String flowID) throws DescopeException;

  FlowResponse importFlow(String flowID, Flow flow, List<Screen> screens) throws DescopeException;

  Theme exportTheme() throws DescopeException;

  Theme importTheme(Theme theme) throws DescopeException;
}
