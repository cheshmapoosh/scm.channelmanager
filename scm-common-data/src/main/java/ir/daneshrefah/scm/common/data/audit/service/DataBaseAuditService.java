package ir.daneshrefah.scm.common.data.audit.service;

import ir.daneshrefah.scm.common.data.audit.config.AuditConfig;
import ir.daneshrefah.scm.common.data.audit.domain.AuditLogEntity;
import ir.daneshrefah.scm.common.data.audit.util.InstanceManager;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataBaseAuditService implements AuditService {

    private final InstanceManager instanceManager;
    @PersistenceContext(unitName = "mainEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void log(AuditEvent auditEvent) {
        AuditLogEntity auditLogEntity = createAuditLogRepository(auditEvent);
        entityManager.persist(auditLogEntity);
    }


    private AuditLogEntity createAuditLogRepository(AuditEvent auditEvent) {
        AuditLogEntity auditLogEntity = new AuditLogEntity();
        auditLogEntity.setRevisionType(auditEvent.getRevisionType());
        auditLogEntity.setClassType(auditEvent.getClassType().getName());
        auditLogEntity.setTypeId(getInstanceId(auditEvent.getInstanceId()));
        auditLogEntity.setValue(getShallowJSON(auditEvent));
        auditEvent.setTimestamp(new Timestamp(System.currentTimeMillis()));
        auditLogEntity.setTimestamp(auditEvent.getTimestamp());
        if (auditEvent.getData() instanceof AbstractDefaultEntity<?> baseEntity) {
            auditLogEntity.setCreator(baseEntity.getCreator());
            auditLogEntity.setModifyBy(baseEntity.getLastEditor());
        }
        return auditLogEntity;
    }

    @SneakyThrows
    private String getShallowJSON(AuditEvent auditEvent) {
        return AuditConfig.getObjectMapper().writeValueAsString(instanceManager.shallowCopy(auditEvent));
    }


    @SneakyThrows
    private String getInstanceId(Object instanceId) {
        if (instanceId instanceof UUID uuid) {
            return uuid.toString();
        } else if (instanceId instanceof Number || instanceId instanceof String){
            return String.valueOf(instanceId);
        }else {
            return AuditConfig.getObjectMapper().writeValueAsString(instanceId);
        }
    }

}
