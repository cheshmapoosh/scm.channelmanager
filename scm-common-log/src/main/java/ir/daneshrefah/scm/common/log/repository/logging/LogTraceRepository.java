package ir.daneshrefah.scm.common.log.repository.logging;


import ir.daneshrefah.scm.common.log.entity.logging.LogPrimaryKey;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LogTraceRepository extends JpaRepository<LogTraceEntity, LogPrimaryKey>, JpaSpecificationExecutor<LogTraceEntity> {
    @Query(value = "SELECT CAST(xmlserialize(xmlagg(xmltext(PAYLOAD)) AS CLOB) AS VARCHAR(32000)) FROM TBL_LOG_TRACE WHERE SPAN_ID = :spanId AND TRACE_ID = :traceId", nativeQuery = true)
    String findAggregatedPayload(@Param("spanId") String spanId, @Param("traceId") String traceId);
}