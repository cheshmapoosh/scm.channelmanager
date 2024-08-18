package ir.daneshrefah.scm.common.data.audit.listener.event;

import ir.daneshrefah.scm.common.data.audit.service.AuditServiceInvocation;
import ir.daneshrefah.scm.common.data.audit.util.JpaMetadataResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditListener implements ApplicationListener<ApplicationAuditEvent> {

    private final AuditServiceInvocation auditServiceInvocation;
    private final JpaMetadataResolver metadataResolver;

    @Override
    public void onApplicationEvent(ApplicationAuditEvent event) {
        metadataResolver.getMetaData(event.getAuditEvent().getClassType())
                .ifPresent(metaData -> {
                    event.getAuditEvent().setMetaData(metaData);
                    auditServiceInvocation.execute(event.getAuditEvent());
                });
    }
}
