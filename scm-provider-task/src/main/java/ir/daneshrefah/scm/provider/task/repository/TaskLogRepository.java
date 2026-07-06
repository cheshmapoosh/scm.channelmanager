package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.entity.TaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLogRepository extends JpaRepository<TaskLogEntity,Long> {}
