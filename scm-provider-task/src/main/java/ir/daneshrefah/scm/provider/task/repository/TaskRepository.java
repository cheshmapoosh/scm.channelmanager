package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {
    Optional<TaskEntity> findByIdAndUserId(Long id, Integer userID);

    List<TaskEntity> findByProcessInstance_Id(Long processInstanceId);

    List<TaskEntity> findByProcessInstance_IdAndSignerAndTaskStatus(Long processInstanceId, Boolean signer, TaskStatusEnum taskStatus);

}
