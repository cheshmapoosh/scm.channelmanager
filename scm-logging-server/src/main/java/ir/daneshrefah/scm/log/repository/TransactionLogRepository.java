package ir.daneshrefah.scm.log.repository;

import ir.daneshrefah.scm.log.entity.AbstractLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogRepository extends JpaRepository<AbstractLogEntity,Long> {
}
