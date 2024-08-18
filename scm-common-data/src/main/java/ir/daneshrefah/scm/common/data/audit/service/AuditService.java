package ir.daneshrefah.scm.common.data.audit.service;

import ir.daneshrefah.scm.common.model.audit.AuditEvent;

public interface AuditService {
    void log(AuditEvent auditEvent);
}
