package ir.daneshrefah.scm.logging.audit;

import ir.daneshrefah.scm.common.data.audit.service.AuditService;
import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import ir.daneshrefah.scm.logging.api.EventProducer;
import org.springframework.stereotype.Service;

@Service
public class FileAuditService implements AuditService {

    @Override
    public void log(AuditEvent auditEvent) {
        EventProducer producer = EventProducer.getInstance();
        producer.sendEvent(auditEvent);
    }
}
