package ir.daneshrefah.scm.task.repository;

import ir.daneshrefah.scm.task.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {
    Optional<TaskEntity> findByIdAndUserId(Long id, Integer userID);
}
