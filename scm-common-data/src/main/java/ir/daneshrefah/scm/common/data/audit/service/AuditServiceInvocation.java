package ir.daneshrefah.scm.common.data.audit.service;

import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceInvocation {

    private final List<AuditService>  auditServices;

    @Async("auditLogThreadPool")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(AuditEvent auditEvent) {
       auditServices.forEach(auditService -> auditService.log(auditEvent));
    }
}
