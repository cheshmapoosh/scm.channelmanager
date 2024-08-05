package ir.daneshrefah.scm.common.data.audit.service;


import ir.daneshrefah.scm.common.data.audit.model.AuditDetails;

public interface AuditService {
    void log(AuditDetails auditDetails);
}
