package ir.daneshrefah.scm.common.data.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.audit.domain.AuditLogEntity;
import ir.daneshrefah.scm.common.data.audit.model.AuditDetails;
import ir.daneshrefah.scm.common.data.audit.util.InstanceManager;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataBaseAuditService implements AuditService {

    @Qualifier("auditObjectMapper")
    private final ObjectMapper objectMapper;
    private final InstanceManager instanceManager;
    @PersistenceContext
    private EntityManager entityManager;

    @Async("auditLogThreadPool")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void log(AuditDetails auditInfo) {
        AuditLogEntity auditLogEntity = createAuditLogRepository(auditInfo);
        entityManager.persist(auditLogEntity);
    }

    private AuditLogEntity createAuditLogRepository(AuditDetails auditInfo) {
        AuditLogEntity auditLogEntity = new AuditLogEntity();
        auditLogEntity.setRevisionType(auditInfo.getRevisionType());
        auditLogEntity.setClassType(auditInfo.getType().getName());
        auditLogEntity.setTypeId(getInstanceId(auditInfo.getInstanceId()));
        auditLogEntity.setValue(getShallowJSON(auditInfo));
        auditLogEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
        if (auditInfo.getData() instanceof AbstractDefaultEntity<?> baseEntity){
            auditLogEntity.setCreator(baseEntity.getCreator());
            auditLogEntity.setModifyBy(baseEntity.getLastEditor());
        }
        return auditLogEntity;
    }

    @SneakyThrows
    private String getShallowJSON(AuditDetails auditDetails) {
        return objectMapper.writeValueAsString(instanceManager.shallowCopy(auditDetails));
    }


    @SneakyThrows
    private String getInstanceId(Object instanceId) {
        if (instanceId instanceof UUID uuid) {
            return uuid.toString();
        } else if (instanceId instanceof Number || instanceId instanceof String){
            return String.valueOf(instanceId);
        }else {
            return objectMapper.writeValueAsString(instanceId);
        }
    }

}
