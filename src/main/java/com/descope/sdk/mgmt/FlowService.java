package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.ManagementFlowOptions;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import java.util.List;
import java.util.Map;

public interface FlowService {
  
  FlowsResponse listFlows() throws DescopeException;

  /**
   * List or search flows.
   *
   * @param ids The IDs of the flows to return, or {@code null} or empty to list all flows
   * @return The matching flows and their total count
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FlowsResponse listFlows(List<String> ids) throws DescopeException;

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

  /**
   * Run a management flow and wait for its result.
   *
   * @param flowId The ID of the management flow to run
   * @param options Optional input, preview mode and tenant (up to 254 characters) for the run, or {@code null}
   * @return The output of the flow
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Map<String, Object> runManagementFlow(String flowId, ManagementFlowOptions options) throws DescopeException;

  /**
   * Run a management flow asynchronously. Use {@link #getManagementFlowAsyncResult(String)} with the
   * returned execution ID to get its result.
   *
   * @param flowId The ID of the management flow to run
   * @param options Optional input, preview mode and tenant (up to 254 characters) for the run, or {@code null}
   * @return The execution ID of the run
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String runManagementFlowAsync(String flowId, ManagementFlowOptions options) throws DescopeException;

  /**
   * Get the result of an asynchronous management flow run, if any.
   *
   * @param executionId The execution ID returned by {@link #runManagementFlowAsync(String, ManagementFlowOptions)}
   * @return The output of the flow
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Map<String, Object> getManagementFlowAsyncResult(String executionId) throws DescopeException;
}
