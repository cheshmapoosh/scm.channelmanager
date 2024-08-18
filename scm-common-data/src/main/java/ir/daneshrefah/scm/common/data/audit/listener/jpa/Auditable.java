package ir.daneshrefah.scm.common.data.audit.listener.jpa;

import ir.daneshrefah.scm.common.data.audit.config.AuditConfig;
import ir.daneshrefah.scm.common.data.audit.listener.event.ApplicationAuditEvent;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultAuditableEntity;
import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import ir.daneshrefah.scm.common.model.audit.constants.RevisionType;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class Auditable {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public ApplicationEventPublisher getApplicationEventPublisher() {
        return applicationEventPublisher;
    }

    @PostPersist
    public void postSave(Object entity) {
        publishAuditEvent(RevisionType.INSERT, entity);
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        publishAuditEvent(RevisionType.UPDATE, entity);
    }

    @PostRemove
    public void postRemove(Object entity) {
        publishAuditEvent(RevisionType.DELETE, entity);
    }

    private void publishAuditEvent(RevisionType revisionType, Object entity) {
        if (entity instanceof AbstractDefaultAuditableEntity<?> abstractDefaultEntity) {
            Auditable auditable = AuditConfig.getApplicationContext().getBean(Auditable.class);
            ApplicationAuditEvent auditEvent = createAuditEvent(revisionType, abstractDefaultEntity);
            auditable.getApplicationEventPublisher().publishEvent(auditEvent);
        }
    }

    public ApplicationAuditEvent createAuditEvent(RevisionType revisionType, AbstractDefaultAuditableEntity<?> abstractDefaultEntity) {
        AuditEvent auditEvent = AuditEvent.builder()
                .data(abstractDefaultEntity)
                .classType(abstractDefaultEntity.getClass())
                .classTypeStr(abstractDefaultEntity.getClass().toString())
                .instanceId(abstractDefaultEntity.getId())
                .revisionType(revisionType)
                .build();
        return new ApplicationAuditEvent(this, auditEvent);
    }
}