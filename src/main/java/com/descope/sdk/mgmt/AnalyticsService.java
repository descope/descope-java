package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.analytics.AnalyticsSearchRequest;
import com.descope.model.analytics.AnalyticsSearchResponse;

/** Provides analytics search capabilities. */
public interface AnalyticsService {
  /**
   * Search summarized analytics data, grouped by the given fields and time periods. All filter fields
   * are optional and analytics may search up to 12 months of history.
   *
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link AnalyticsSearchResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  AnalyticsSearchResponse search(AnalyticsSearchRequest request) throws DescopeException;
}
