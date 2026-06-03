package ir.daneshrefah.scm.core.integration.audit;

import java.time.Instant;

public record AuditEvent(
        Instant timestamp,
        String traceId,
        String spanId,
        String correlationId,
        String gatewayName,
        String channelCode,
        String serviceCode,
        String serviceVersion,
        String operationName,
        String phase,
        String status,
        String errorCode,
        String errorMessage,
        String routeId,
        String exchangeId) {
}
