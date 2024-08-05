package ir.daneshrefah.scm.common.data.audit.listener.jpa;

import ir.daneshrefah.scm.common.data.audit.listener.event.AuditEvent;
import ir.daneshrefah.scm.common.data.audit.model.AuditDetails;
import ir.daneshrefah.scm.common.data.audit.model.constants.RevisionType;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class Auditable {

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostPersist
    public void postSave(AbstractDefaultEntity<?> abstractDefaultEntity) {
        applicationEventPublisher.publishEvent(createAuditEvent(RevisionType.INSERT, abstractDefaultEntity));
    }

    @PostUpdate
    public void postUpdate(AbstractDefaultEntity<?> abstractDefaultEntity) {
        applicationEventPublisher.publishEvent(createAuditEvent(RevisionType.UPDATE, abstractDefaultEntity));
    }

    @PostRemove
    public void postRemove(AbstractDefaultEntity<?> abstractDefaultEntity) {
        applicationEventPublisher.publishEvent(createAuditEvent(RevisionType.DELETE, abstractDefaultEntity));
    }

    private AuditEvent createAuditEvent(RevisionType revisionType, AbstractDefaultEntity<?> abstractDefaultEntity) {
        AuditDetails auditInfo = new AuditDetails()
                .setData(abstractDefaultEntity)
                .setType(abstractDefaultEntity.getClass())
                .setInstanceId(abstractDefaultEntity.getId())
                .setRevisionType(revisionType);
        return new AuditEvent(this, auditInfo);
    }

}
