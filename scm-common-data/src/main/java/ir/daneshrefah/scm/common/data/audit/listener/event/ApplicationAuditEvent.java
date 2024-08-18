package ir.daneshrefah.scm.common.data.audit.listener.event;

import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationAuditEvent extends ApplicationEvent {

    private final AuditEvent auditEvent;

    public ApplicationAuditEvent(Object source, AuditEvent auditEvent) {
        super(source);
        this.auditEvent = auditEvent;
    }
}
