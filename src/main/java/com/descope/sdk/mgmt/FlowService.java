package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.ExportedTheme;
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

  /**
   * Export the project theme, including its styles and the project entities they reference.
   *
   * @return {@link ExportedTheme} the project theme
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ExportedTheme exportProjectTheme() throws DescopeException;

  /**
   * Import a theme to the project, usually one returned by {@link #exportProjectTheme()}. The styles in the
   * given theme are created or overwritten, and every other style in the project is left as it is.
   *
   * @param theme The theme to import, its styles are required
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void importProjectTheme(ExportedTheme theme) throws DescopeException;
}
