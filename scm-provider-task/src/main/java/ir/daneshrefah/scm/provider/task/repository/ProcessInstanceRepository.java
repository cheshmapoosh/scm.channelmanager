package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstanceEntity, Long>, JpaSpecificationExecutor<ProcessInstanceEntity> {
}
