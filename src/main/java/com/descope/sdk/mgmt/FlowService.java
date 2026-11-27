package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.ManagementFlowOptions;
import com.descope.model.flow.ManagementFlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import java.util.List;

public interface FlowService {
  
  FlowsResponse listFlows() throws DescopeException;

  /**
   * List the flows in the project, optionally only those with the given IDs.
   *
   * @param ids The IDs of the flows to list, or {@code null} to list all flows
   * @return The matching flows and their total count
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  FlowsResponse listFlows(List<String> ids) throws DescopeException;

  /**
   * Run a management flow and wait for its output.
   *
   * @param flowId The ID of the flow to run
   * @param options Optional input, preview mode and tenant for the run, or {@code null}
   * @return The flow execution output
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ManagementFlowResponse runManagementFlow(String flowId, ManagementFlowOptions options) throws DescopeException;

  /**
   * Start a management flow without waiting for it to finish.
   *
   * @param flowId The ID of the flow to run
   * @param options Optional input, preview mode and tenant for the run, or {@code null}
   * @return The execution ID, to pass to {@link #getManagementFlowAsyncResult(String)}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String runManagementFlowAsync(String flowId, ManagementFlowOptions options) throws DescopeException;

  /**
   * Get the output of a management flow started with {@link #runManagementFlowAsync(String, ManagementFlowOptions)}.
   *
   * @param executionId The execution ID returned when the flow was started
   * @return The flow execution output, in the same shape as {@link #runManagementFlow(String, ManagementFlowOptions)}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ManagementFlowResponse getManagementFlowAsyncResult(String executionId) throws DescopeException;

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
