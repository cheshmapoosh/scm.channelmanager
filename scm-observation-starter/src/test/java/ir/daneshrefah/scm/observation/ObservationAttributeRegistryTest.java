package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.log.LogAttribute;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationAttributeRegistryTest {
    @Test
    void commonRegistryContainsOnlyCommonCatalogs() {
        ObservationAttributeRegistry registry = ObservationAttributeRegistry.commonOnly();

        assertTrue(registry.contains(ObservationStream.LOG, "log.level"));
        assertTrue(registry.contains(ObservationStream.TRACE, "parent.span.id"));
        assertTrue(registry.contains(ObservationStream.AUDIT, "audit.type"));
        assertTrue(registry.contains(ObservationStream.METRIC, "operation_code"));
        assertTrue(registry.contains(ObservationStream.TRACE, "http.method"));
        assertFalse(registry.contains(ObservationStream.TRACE, "span.role"));
    }

    @Test
    void registryLoadsHostContributorAttributes() {
        ObservationAttributeKey<String> hostKey = LogAttribute.keyword(
                "uaa.auth.method", "scm-uaa", ObservationAttributePresence.EVENT_OPTIONAL, "Authentication method.");

        ObservationAttributeRegistry registry = new ObservationAttributeRegistry(List.of(() -> List.of(hostKey)));

        assertTrue(registry.contains(ObservationStream.LOG, "uaa.auth.method"));
    }

    @Test
    void incompatibleDuplicateFailsFast() {
        ObservationAttributeKey<String> incompatible = TraceAttribute.keyword(
                "message", "scm-host", ObservationAttributePresence.EVENT_OPTIONAL, "Conflicting message metadata.");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ObservationAttributeRegistry(List.of(() -> List.of(incompatible))));
        assertTrue(exception.getMessage().contains("TRACE:message"));
        assertTrue(exception.getMessage().contains("existing={"));
        assertTrue(exception.getMessage().contains("incoming={"));
        assertTrue(exception.getMessage().contains("owner=common"));
        assertTrue(exception.getMessage().contains("owner=scm-host"));
    }

    @Test
    void duplicateNameWithDifferentOwnerFailsFast() {
        ObservationAttributeKey<String> ownerA = TraceAttribute.keyword(
                "duplicate.owner.test", "module-a", ObservationAttributePresence.EVENT_OPTIONAL, "Module A.");
        ObservationAttributeKey<String> ownerB = TraceAttribute.keyword(
                "duplicate.owner.test", "module-b", ObservationAttributePresence.EVENT_OPTIONAL, "Module B.");

        assertThrows(IllegalStateException.class,
                () -> new ObservationAttributeRegistry(List.of(() -> List.of(ownerA, ownerB))));
    }

    @Test
    void duplicateNameWithIdenticalMetadataPasses() {
        ObservationAttributeKey<String> first = TraceAttribute.keyword(
                "http.method", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP method.");
        ObservationAttributeKey<String> second = TraceAttribute.keyword(
                "http.method", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP method.");

        ObservationAttributeRegistry registry = assertDoesNotThrow(
                () -> new ObservationAttributeRegistry(List.of(() -> List.of(first, second))));

        assertTrue(registry.contains(ObservationStream.TRACE, "http.method"));
    }
}
