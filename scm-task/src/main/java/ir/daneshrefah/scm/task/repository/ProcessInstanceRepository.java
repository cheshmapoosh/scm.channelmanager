package ir.daneshrefah.scm.task.repository;

import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstanceEntity, Long>, JpaSpecificationExecutor<ProcessInstanceEntity> {

    Optional<ProcessInstanceEntity> findByIdAndConfirmUserId(Long id, Integer confirmUserId);
}
