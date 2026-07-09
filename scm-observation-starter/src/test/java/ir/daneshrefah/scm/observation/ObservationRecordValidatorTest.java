package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.audit.ServiceExecuteAuditAttributes;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObservationRecordValidatorTest {
    private final ObservationRecordValidator validator =
            new ObservationRecordValidator(ObservationAttributeRegistry.commonOnly());

    @Test
    void auditAcceptsOnlyTheTwoCommonModels() {
        assertDoesNotThrow(() -> validator.validate(
                ObservationStream.AUDIT, ObservationRecordKind.EVENT, false,
                auditDocument(ChangeEntityAuditAttributes.TYPE_VALUE)));
        assertDoesNotThrow(() -> validator.validate(
                ObservationStream.AUDIT, ObservationRecordKind.EVENT, false,
                auditDocument(ServiceExecuteAuditAttributes.TYPE_VALUE)));

        assertThrows(IllegalStateException.class, () -> validator.validate(
                ObservationStream.AUDIT, ObservationRecordKind.EVENT, false,
                auditDocument("CUSTOM_AUDIT")));
    }

    @Test
    void missingOrBlankParentSpanMeansRootSpan() {
        Map<String, Object> absentParent = traceDocument();
        assertDoesNotThrow(() -> validator.validate(
                ObservationStream.TRACE, ObservationRecordKind.EVENT, false, absentParent));

        Map<String, Object> nullParent = traceDocument();
        nullParent.put("parent.span.id", null);
        assertDoesNotThrow(() -> validator.validate(
                ObservationStream.TRACE, ObservationRecordKind.EVENT, false, nullParent));

        Map<String, Object> blankParent = traceDocument();
        blankParent.put("parent.span.id", "  ");
        assertDoesNotThrow(() -> validator.validate(
                ObservationStream.TRACE, ObservationRecordKind.EVENT, false, blankParent));
    }

    private Map<String, Object> auditDocument(String auditType) {
        Map<String, Object> document = baseDocument();
        document.put("audit.type", auditType);
        return document;
    }

    private Map<String, Object> traceDocument() {
        Map<String, Object> document = baseDocument();
        document.put("trace.id", "trace-1");
        document.put("span.id", "span-1");
        document.put("span.name", "test");
        document.put("span.kind", "internal");
        document.put("span.start_time", "2026-06-27T00:00:00Z");
        document.put("span.end_time", "2026-06-27T00:00:01Z");
        document.put("span.duration_ms", 1000L);
        return document;
    }

    private Map<String, Object> baseDocument() {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("@timestamp", "2026-06-27T00:00:01Z");
        document.put("message", "test");
        document.put("event.stream", "test");
        document.put("scm.observation.target.namespace", "test");
        document.put("scm.observation.target.index", "test-index");
        document.put("scm.platform", "scm");
        document.put("service.name", "test-service");
        document.put("deployment.environment", "test");
        document.put("correlation.id", "correlation-1");
        document.put("correlation.type", CorrelationType.OPERATION.value());
        document.put("event.category", "test");
        document.put("event.action", "test.execute");
        document.put("event.outcome", "success");
        return document;
    }
}
