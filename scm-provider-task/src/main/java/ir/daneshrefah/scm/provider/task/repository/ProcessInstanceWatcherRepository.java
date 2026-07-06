package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessInstanceWatcherRepository extends JpaRepository<ProcessInstanceWatcherEntity, Long>{

}
