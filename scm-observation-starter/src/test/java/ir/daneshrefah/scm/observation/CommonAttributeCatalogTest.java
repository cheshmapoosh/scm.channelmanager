package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.audit.ServiceExecuteAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.log.CommonLogAttributes;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonAttributeCatalogTest {
    @Test
    void logCatalogContainsOnlyTrueCommonFields() {
        assertEquals(Set.of(
                "@timestamp", "log.level", "log.logger", "process.thread.name", "message",
                "deployment.service.name", "deployment.service.version", "deployment.environment", "scm.runtime",
                "correlation.id", "correlation.type", "event.category", "event.action", "event.outcome",
                "trace.id", "span.id", "error.type", "error.message", "error.stack_trace", "error.code", "error.category"
        ), names(CommonLogAttributes.attributes()));
    }

    @Test
    void traceCatalogContainsOnlyTrueCommonFields() {
        assertEquals(Set.of(
                "@timestamp", "message",
                "deployment.service.name", "deployment.service.version", "deployment.environment", "scm.runtime",
                "correlation.id", "correlation.type", "trace.id", "span.id", "parent.span.id",
                "span.name", "span.kind", "span.start_time", "span.end_time", "span.duration_ms",
                "event.category", "event.action", "event.outcome",
                "error.type", "error.message", "error.stack_trace", "error.code", "error.category"
        ), names(CommonTraceAttributes.attributes()));
    }

    @Test
    void auditCatalogsExposeOnlyTheTwoApprovedModels() {
        Set<String> changeNames = names(ChangeEntityAuditAttributes.attributes());
        Set<String> serviceNames = names(ServiceExecuteAuditAttributes.attributes());

        assertTrue(changeNames.containsAll(Set.of(
                "audit.type", "actor.type", "actor.id", "actor.username.masked",
                "entity.type", "entity.id", "entity.code", "change.action", "change.field",
                "change.old.value.masked", "change.new.value.masked")));
        assertTrue(serviceNames.containsAll(Set.of(
                "audit.type", "actor.type", "actor.id", "actor.username.masked",
                "resource.type", "resource.id", "channel.code", "scm.service.code", "scm.operation.code",
                "request.id", "message.sequence.id", "client.ip", "status.code")));
    }

    @Test
    void metricCatalogContainsOnlyApprovedLowCardinalityTags() {
        assertEquals(Set.of(
                "service_name", "environment", "channel_code", "operation_code", "outcome", "error_code", "status_code"
        ), names(CommonMetricTags.attributes()));
    }

    private Set<String> names(Iterable<ObservationAttributeKey<?>> attributes) {
        return java.util.stream.StreamSupport.stream(attributes.spliterator(), false)
                .map(ObservationAttributeKey::name)
                .collect(Collectors.toSet());
    }
}
