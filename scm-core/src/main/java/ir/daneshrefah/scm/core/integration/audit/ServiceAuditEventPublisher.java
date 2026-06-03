package ir.daneshrefah.scm.core.integration.audit;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceAuditEventPublisher {
    private static final String SERVICE_PHASE = "SERVICE";

    private final AuditEventWriter auditEventWriter;
    private final ScmExchangeMdc scmExchangeMdc;

    public void recordSuccess(Exchange exchange) {
        write(exchange, "SUCCESS", null, null);
    }

    public void recordFailure(Exchange exchange, Exception exception) {
        Error error = firstFaultError(exchange);
        String errorCode = error != null ? error.getErrorCode() : exception != null ? exception.getClass().getSimpleName() : null;
        String errorMessage = error != null ? error.getMessage() : exception != null ? exception.getMessage() : null;
        write(exchange, "FAILED", errorCode, errorMessage);
    }

    private void write(Exchange exchange, String status, String errorCode, String errorMessage) {
        Map<String, String> fields = scmExchangeMdc.put(exchange);
        try {
            auditEventWriter.write(new AuditEvent(
                    Instant.now(),
                    fields.get("traceId"),
                    fields.get("spanId"),
                    fields.get("correlationId"),
                    fields.get("gatewayName"),
                    fields.get("channelCode"),
                    fields.get("serviceCode"),
                    fields.get("serviceVersion"),
                    fields.get("operationName"),
                    SERVICE_PHASE,
                    status,
                    errorCode,
                    errorMessage,
                    fields.get("routeId"),
                    fields.get("exchangeId")));
        } catch (Exception e) {
            log.warn("Service audit could not write audit event routeId={} exchangeId={}",
                    exchange.getFromRouteId(), exchange.getExchangeId(), e);
        }
    }

    private Error firstFaultError(Exchange exchange) {
        ScmFault fault = exchange.getMessage().getBody(ScmFault.class);
        if (fault == null || fault.getErrors() == null || fault.getErrors().isEmpty()) {
            return null;
        }
        return fault.getErrors().getFirst();
    }
}
