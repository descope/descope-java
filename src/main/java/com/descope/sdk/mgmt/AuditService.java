package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.audit.AuditSearchResponse;

/** Provides audit records search capabilities. */
public interface AuditService {
  /**
   * Search the audit trail and retrieve audit records based on the given filter. All filter fields
   * are optional and audit may search up to 30 days of history.
   *
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link AuditSearchResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  AuditSearchResponse search(AuditSearchRequest request) throws DescopeException;
}
