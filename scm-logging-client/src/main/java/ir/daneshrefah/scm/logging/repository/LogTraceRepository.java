package ir.daneshrefah.scm.logging.repository;

import ir.daneshrefah.scm.logging.entity.LogPrimaryKey;
import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.model.LogTraceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface LogTraceRepository extends JpaRepository<LogTraceEntity, LogPrimaryKey>{

    @Query("SELECT new ir.daneshrefah.scm.logging.model.LogTraceResponse(" +
            "t.logPrimaryKey.spanId,t.logPrimaryKey.traceId, t.channelCode, t.terminalCode, t.clientId, t.correlationId, t.clientCorrelationId, " +
            "t.messageId, t.statusCode, t.nickname, t.username, t.delegatorUsername, t.endPoint, " +
            "t.amount, t.accountNo, t.cardNo, t.startTime, t.endTime) " +
            "FROM LogTraceEntity t " +
            "WHERE (:channelCode IS NULL OR t.channelCode = :channelCode) " +
            "AND (:terminalCode IS NULL OR t.terminalCode = :terminalCode) " +
            "AND (:clientId IS NULL OR t.clientId = :clientId) " +
            "AND (:correlationId IS NULL OR t.correlationId = :correlationId) " +
            "AND (:clientCorrelationId IS NULL OR t.clientCorrelationId = :clientCorrelationId) " +
            "AND (:messageId IS NULL OR t.messageId = :messageId) " +
            "AND (:statusCode IS NULL OR t.statusCode = :statusCode) " +
            "AND (:nickname IS NULL OR t.nickname = :nickname) " +
            "AND (:username IS NULL OR t.username = :username) " +
            "AND (:delegatorUsername IS NULL OR t.delegatorUsername = :delegatorUsername) " +
            "AND (:endPoint IS NULL OR t.endPoint = :endPoint) " +
            "AND (:amount IS NULL OR t.amount = :amount) " +
            "AND (:accountNo IS NULL OR t.accountNo = :accountNo) " +
            "AND (:cardNo IS NULL OR t.cardNo = :cardNo) " +
            "AND (:startTime IS NULL OR t.startTime >= :startTime) " +
            "AND (:endTime IS NULL OR t.endTime <= :endTime)")
    Page<LogTraceResponse> findAll(@Param("channelCode") String channelCode,
                                   @Param("terminalCode") String terminalCode,
                                   @Param("clientId") String clientId,
                                   @Param("correlationId") String correlationId,
                                   @Param("clientCorrelationId") String clientCorrelationId,
                                   @Param("messageId") String messageId,
                                   @Param("statusCode") Integer statusCode,
                                   @Param("nickname") String nickname,
                                   @Param("username") String username,
                                   @Param("delegatorUsername") String delegatorUsername,
                                   @Param("endPoint") String endPoint,
                                   @Param("amount") String amount,
                                   @Param("accountNo") String accountNo,
                                   @Param("cardNo") String cardNo,
                                   @Param("startTime") Date startTime,
                                   @Param("endTime") Date endTime,
                                   Pageable pageable);

}