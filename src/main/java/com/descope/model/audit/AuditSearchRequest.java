package com.descope.model.audit;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuditSearchRequest options to filter which audit records to retrieve.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSearchRequest {
  /** List of user IDs to filter by. */
  List<String> userIds;
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
  /** List of remote addresses to filter by. */
  List<String> remoteAddresses;
  /** List of login IDs to filter by. */
  List<String> loginIds;
  /** List of tenants to filter by. */
  List<String> tenants;
  /** Should audits without any tenants always be included. */
  boolean noTenants;
  /** Free text search across all fields. */
  String text;
  /** Retrieve records newer than given time. Limited to no older than 30 days. */
  Instant from;
  /** Retrieve records older than given time. */
  Instant to;
}
