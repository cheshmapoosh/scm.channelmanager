package ir.daneshrefah.scm.common.log.repository.message;


import ir.daneshrefah.scm.common.log.entity.logging.LogPrimaryKey;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.entity.message.MessageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLogEntity, Long>, JpaSpecificationExecutor<MessageLogEntity> {


}