package ir.daneshrefah.scm.common.data.audit.listener.event;

import ir.daneshrefah.scm.common.data.audit.model.AuditDetails;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {

    private final AuditDetails auditInfo;

    public AuditEvent(Object source, AuditDetails auditDetails) {
        super(source);
        this.auditInfo = auditDetails;
    }
}
