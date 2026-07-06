package ir.daneshrefah.scm.task.repository;

import ir.daneshrefah.scm.task.entity.TaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLogRepository extends JpaRepository<TaskLogEntity,Long> {}
