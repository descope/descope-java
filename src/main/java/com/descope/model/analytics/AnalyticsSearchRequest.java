package com.descope.model.analytics;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AnalyticsSearchRequest options to filter and group the analytics to retrieve. All fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSearchRequest {
  /** Retrieve analytics newer than given time. */
  Instant from;
  /** Retrieve analytics older than given time. */
  Instant to;
  /** List of actions to filter by. */
  List<String> actions;
  /** List of actions to exclude. */
  List<String> excludedActions;
  /**
   * List of devices to filter by.
   * Current devices supported are "Bot"/"Mobile"/"Desktop"/"Tablet"/"Unknown"
   */
  List<String> devices;
  /**
   * List of methods to filter by.
   * Current auth methods are "otp"/"totp"/"magiclink"/"oauth"/"saml"/"password"
   */
  List<String> methods;
  /** List of geos to filter by. Geo is currently country code like "US", "IL", etc. */
  List<String> geos;
  /** List of tenants to filter by. */
  List<String> tenants;
  /** Group summarized results by action. */
  Boolean groupByAction;
  /** Group summarized results by device. */
  Boolean groupByDevice;
  /** Group summarized results by method. */
  Boolean groupByMethod;
  /** Group summarized results by geo. */
  Boolean groupByGeo;
  /** Group summarized results by tenant. */
  Boolean groupByTenant;
  /** Group summarized results by referrer. */
  Boolean groupByReferrer;
  /**
   * How to group the dates. Possible values are "h" for hour, "d" for day, "w" for week, "m" for month
   * and "q" for quarter.
   */
  String groupByCreated;
}
