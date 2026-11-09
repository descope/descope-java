package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.analytics.AnalyticsSearchRequest;
import com.descope.model.analytics.AnalyticsSearchResponse;

/** Provides analytics search capabilities. */
public interface AnalyticsService {
  /**
   * Search analytics (summarized) data grouped by time periods, based on the given filter.
   *
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link AnalyticsSearchResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  AnalyticsSearchResponse search(AnalyticsSearchRequest request) throws DescopeException;
}
