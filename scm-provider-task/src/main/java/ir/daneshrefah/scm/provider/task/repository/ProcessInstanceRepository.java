package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstanceEntity, Long>, JpaSpecificationExecutor<ProcessInstanceEntity> {
    Optional<ProcessInstanceEntity> findByCorrelationId(String correlationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select process from ProcessInstanceEntity process where process.id = :processId")
    Optional<ProcessInstanceEntity> findByIdForWorkflowMutation(
            @Param("processId") Long processId
    );
}
