package com.descope.model.analytics;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AnalyticsSearchRequest options to filter and group the analytics data to retrieve.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSearchRequest {
  /** Group by created time: hour. */
  public static final String GROUP_BY_CREATED_HOUR = "h";
  /** Group by created time: day. */
  public static final String GROUP_BY_CREATED_DAY = "d";
  /** Group by created time: week. */
  public static final String GROUP_BY_CREATED_WEEK = "w";
  /** Group by created time: month. */
  public static final String GROUP_BY_CREATED_MONTH = "m";
  /** Group by created time: quarter. */
  public static final String GROUP_BY_CREATED_QUARTER = "q";

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
  /** Retrieve records newer than given time. Limited to no older than 12 months. */
  Instant from;
  /** Retrieve records older than given time. */
  Instant to;
  /** Group results by action. */
  Boolean groupByAction;
  /** Group results by device. */
  Boolean groupByDevice;
  /** Group results by method. */
  Boolean groupByMethod;
  /** Group results by geo. */
  Boolean groupByGeo;
  /** Group results by tenant. */
  Boolean groupByTenant;
  /** Group results by referrer. */
  Boolean groupByReferrer;
  /** Group results by created time period, one of the GROUP_BY_CREATED_* constants. */
  String groupByCreated;
}
