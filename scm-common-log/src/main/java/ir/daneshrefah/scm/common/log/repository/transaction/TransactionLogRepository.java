package ir.daneshrefah.scm.common.log.repository.transaction;

import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
//import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLogEntity, Long>, JpaSpecificationExecutor<TransactionLogEntity> { }
