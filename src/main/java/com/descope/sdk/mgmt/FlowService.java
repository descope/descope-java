package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.ExportedFlow;
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

  /**
   * Export a flow and its screens by the flow ID, in the format {@link #importFlowV2} accepts.
   *
   * @param flowID The ID of the flow to export
   * @return The exported flow
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ExportedFlow exportFlowV2(String flowID) throws DescopeException;

  /**
   * Import a flow and its screens as the given flow ID, overriding the existing flow. The flow ID
   * overrides the one set in the flow. IMPORTANT: This action is irreversible.
   *
   * @param flowID The ID to import the flow as
   * @param flow The flow to import, usually as returned by {@link #exportFlowV2}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void importFlowV2(String flowID, ExportedFlow flow) throws DescopeException;

  /**
   * Import a flow and its screens as the given flow ID, overriding the existing flow. The flow ID
   * overrides the one set in the flow. IMPORTANT: This action is irreversible.
   *
   * @param flowID The ID to import the flow as
   * @param flow The flow to import, usually as returned by {@link #exportFlowV2}
   * @param skipRender Optional, the import's skipRender option, omitted when null
   * @param changelog Optional, the import's changelog, omitted when null
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void importFlowV2(String flowID, ExportedFlow flow, Boolean skipRender, String changelog)
      throws DescopeException;

  Theme exportTheme() throws DescopeException;

  Theme importTheme(Theme theme) throws DescopeException;
}
