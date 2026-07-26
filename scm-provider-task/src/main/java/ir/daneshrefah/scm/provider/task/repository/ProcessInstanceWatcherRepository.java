package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessInstanceWatcherRepository extends JpaRepository<ProcessInstanceWatcherEntity, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select watcher
              from ProcessInstanceWatcherEntity watcher
             where watcher.processInstance.id = :processId
               and watcher.type = :type
               and watcher.rowNo = :rowNo
            """)
    Optional<ProcessInstanceWatcherEntity> findForWorkflowMutation(
            @Param("processId") Long processId,
            @Param("type") ProcessWatcherEnum type,
            @Param("rowNo") Integer rowNo
    );

    List<ProcessInstanceWatcherEntity> findAllByTypeAndRowNo(
            ProcessWatcherEnum type,
            Integer rowNo
    );

}
