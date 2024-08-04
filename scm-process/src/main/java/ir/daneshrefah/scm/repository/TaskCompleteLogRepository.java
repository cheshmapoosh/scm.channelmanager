package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.TaskCompleteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCompleteLogRepository extends JpaRepository<TaskCompleteLog,Long> {
}
