package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.audit.AuditRecord;
import com.descope.model.audit.AuditSearchRequest;
import java.util.List;

/** Provides audit records search capabilities. */
public interface AuditService {
    /**
     * Search the audit trail and retrieve audit records based on the given filter.
     * All filter fields are optional and audit may search up to 30 days of history.
     *
     * @param request request is optional, and if provided, all attributes within it
     *                are optional.
     * @return {@link AuditRecord List<AuditRecord>}
     * @throws DescopeException If there occurs any exception, a subtype of this
     *                          exception will be
     *                          thrown.
     */
    List<AuditRecord> search(AuditSearchRequest request) throws DescopeException;
}
