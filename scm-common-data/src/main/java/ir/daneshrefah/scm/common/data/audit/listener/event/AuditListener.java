package ir.daneshrefah.scm.common.data.audit.listener.event;

import ir.daneshrefah.scm.common.data.audit.service.AuditService;
import ir.daneshrefah.scm.common.data.audit.util.JpaMetadataResolver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditListener implements ApplicationListener<AuditEvent> {

    private final AuditService dataBaseAuditService;
    private final JpaMetadataResolver metadataResolver;

    @Override
    @SneakyThrows
    public void onApplicationEvent(AuditEvent event) {
        metadataResolver.getMetaData(event.getAuditInfo().getType())
                .ifPresent(metaData -> {
                        event.getAuditInfo().setMetaData(metaData);
                        dataBaseAuditService.log(event.getAuditInfo());
                });
    }
}
