package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.log.LogAttribute;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        assertFalse(registry.contains(ObservationStream.TRACE, "http.method"));
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

        assertThrows(IllegalStateException.class,
                () -> new ObservationAttributeRegistry(List.of(() -> List.of(incompatible))));
    }
}
