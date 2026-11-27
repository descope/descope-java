package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.TenantTheme;
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
   * Get the styles a tenant overrides the project theme with. The logo is returned as a base64 data URI, so the
   * result can be passed to {@link #setTenantTheme(String, TenantTheme)} unchanged.
   *
   * @param tenantId The tenant ID
   * @return The tenant's theme
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown. A not found
   *     error is thrown when the tenant has no theme of its own.
   */
  TenantTheme getTenantTheme(String tenantId) throws DescopeException;

  /**
   * Create or replace the styles a tenant overrides the project theme with. The given styles replace the tenant's
   * stored ones whole. A logo must be given as a base64 data URI.
   *
   * @param tenantId The tenant ID
   * @param theme The tenant's theme, with at least one style flavor
   * @return The stored tenant theme
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  TenantTheme setTenantTheme(String tenantId, TenantTheme theme) throws DescopeException;

  /**
   * Delete a tenant's theme, so flows for that tenant render with the project theme again.
   *
   * @param tenantId The tenant ID
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown. A not found
   *     error is thrown when the tenant has no theme of its own.
   */
  void deleteTenantTheme(String tenantId) throws DescopeException;
}
