package ir.daneshrefah.scm.core.integration.audit;

public interface AuditEventWriter {
    void write(AuditEvent event);
}
