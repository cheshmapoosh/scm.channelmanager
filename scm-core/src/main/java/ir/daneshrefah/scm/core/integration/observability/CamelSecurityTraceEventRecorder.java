package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.uaa.starter.security.event.ScmSecurityEventType;
import org.apache.camel.Exchange;

import java.util.Map;

/**
 * Optional web-owned bridge for attaching a safe security event to an explicit Camel layer scope.
 */
@FunctionalInterface
public interface CamelSecurityTraceEventRecorder {
    void record(
            Exchange exchange,
            String layer,
            ScmSecurityEventType eventType,
            Map<String, ?> attributes
    );
}
