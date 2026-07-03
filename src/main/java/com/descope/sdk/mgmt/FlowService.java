package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import java.util.List;

public interface FlowService {
  
  FlowsResponse listFlows() throws DescopeException;

  /**
   * Delete flows by their IDs.
   *
   * @param flowIds The IDs of the flows to delete
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteFlows(List<String> flowIds) throws DescopeException;

  FlowResponse exportFlow(String flowID) throws DescopeException;

  FlowResponse importFlow(String flowID, Flow flow, List<Screen> screens) throws DescopeException;

  Theme exportTheme() throws DescopeException;

  Theme importTheme(Theme theme) throws DescopeException;
}
