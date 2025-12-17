package ir.daneshrefah.scm.common.log.repository.logging;


import ir.daneshrefah.scm.common.log.entity.logging.LogPrimaryKey;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LogTraceRepository extends JpaRepository<LogTraceEntity, LogPrimaryKey>, JpaSpecificationExecutor<LogTraceEntity> {
    @Query(value = "SELECT CAST(xmlserialize(xmlagg(xmltext(PAYLOAD)) AS CLOB) AS VARCHAR(32000)) FROM TBL_LOG_TRACE WHERE SPAN_ID = :spanId AND TRACE_ID = :traceId", nativeQuery = true)
    String findAggregatedPayload(@Param("spanId") String spanId, @Param("traceId") String traceId);

    @Query(value = """
    SELECT 1 FROM REF.TBL_LOG_TRACE 
    WHERE ROW_NO = :rowNo 
      AND SPAN_ID = :spanId 
      AND TRACE_ID = :traceId 
    FETCH FIRST 1 ROW ONLY
    """, nativeQuery = true)
    Integer exists(
            @Param("rowNo") int rowNo,
            @Param("spanId") String spanId,
            @Param("traceId") String traceId);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE REF.TBL_LOG_TRACE 
        SET 
            CHANNEL_CODE = :#{#e.channelCode},
            TERMINAL_CODE = :#{#e.terminalCode},
            CLIENT_ID = :#{#e.clientId},
            CORROLATION_ID = :#{#e.correlationId},
            CLIENT_CORROLATION_ID = :#{#e.clientCorrelationId},
            FLOW_ID = :#{#e.flowId},
            MESSAGE_ID = :#{#e.messageId},
            SPAN_STATUS = :#{#e.spanStatus},
            SERVICE_CODE = :#{#e.serviceCode},
            NICKNAME = :#{#e.nickname},
            USERNAME = :#{#e.username},
            DELEGATOR_USERNAME = :#{#e.delegatorUsername},
            DELEGATOR_NICKNAME = :#{#e.delegatorNickname},
            HOST_ADDRESS = :#{#e.hostAddress},
            SPAN_KIND = :#{#e.spanKind},
            MESSAGE_STATUS = :#{#e.messageStatus},
            SPAN_NAME = :#{#e.spanName},
            START_TIME = :#{#e.startTime},
            END_TIME = :#{#e.endTime},
            CLIENT_IP_ADDRESS = :#{#e.clientIpAddress},
            VERSION = :#{#e.version},
            PROVIDER_CODE = :#{#e.providerCode},
            PROVIDER_RESPONSE_CODE = :#{#e.providerResponseCode},
            RESPONSE_STATUS_CODE = :#{#e.statusCode},
            EXCEPTION_CLASS_NAME = :#{#e.exceptionClassName},
            ENDPOINT = :#{#e.endPoint},
            AMOUNT = :#{#e.amount},
            ACCOUNT_NO = :#{#e.accountNo},
            CARD_NO = :#{#e.cardNo},
            PARENT_SPAN_ID = :#{#e.parentSpanId},
            PAYLOAD = :#{#e.payload}
        WHERE 
            ROW_NO = :#{#e.logPrimaryKey.rowNo} AND 
            SPAN_ID = :#{#e.logPrimaryKey.spanId} AND
            TRACE_ID = :#{#e.logPrimaryKey.traceId}
        """, nativeQuery = true)
    void update(@Param("e") LogTraceEntity e);


    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO REF.TBL_LOG_TRACE 
        (ROW_NO, SPAN_ID, TRACE_ID, CHANNEL_CODE, TERMINAL_CODE, CLIENT_ID, 
         CORROLATION_ID, CLIENT_CORROLATION_ID, FLOW_ID, MESSAGE_ID, SPAN_STATUS, 
         SERVICE_CODE, NICKNAME, USERNAME, DELEGATOR_USERNAME, DELEGATOR_NICKNAME, 
         HOST_ADDRESS, SPAN_KIND, MESSAGE_STATUS, SPAN_NAME, START_TIME, END_TIME, 
         CLIENT_IP_ADDRESS, VERSION, PROVIDER_CODE, PROVIDER_RESPONSE_CODE, 
         RESPONSE_STATUS_CODE, EXCEPTION_CLASS_NAME, ENDPOINT, AMOUNT, ACCOUNT_NO, 
         CARD_NO, PARENT_SPAN_ID, PAYLOAD, ARCHIVE_NO)
        VALUES 
        (:#{#e.logPrimaryKey.rowNo}, :#{#e.logPrimaryKey.spanId}, :#{#e.logPrimaryKey.traceId},
         :#{#e.channelCode}, :#{#e.terminalCode}, :#{#e.clientId},
         :#{#e.correlationId}, :#{#e.clientCorrelationId}, :#{#e.flowId},
         :#{#e.messageId}, :#{#e.spanStatus}, :#{#e.serviceCode},
         :#{#e.nickname}, :#{#e.username}, :#{#e.delegatorUsername}, 
         :#{#e.delegatorNickname}, :#{#e.hostAddress}, :#{#e.spanKind},
         :#{#e.messageStatus}, :#{#e.spanName}, :#{#e.startTime}, :#{#e.endTime},
         :#{#e.clientIpAddress}, :#{#e.version}, :#{#e.providerCode},
         :#{#e.providerResponseCode}, :#{#e.statusCode}, 
         :#{#e.exceptionClassName}, :#{#e.endPoint}, :#{#e.amount},
         :#{#e.accountNo}, :#{#e.cardNo}, :#{#e.parentSpanId}, 
         :#{#e.payload}, :#{#e.archiveNo})
        """, nativeQuery = true)
    void insert(@Param("e") LogTraceEntity e);
}