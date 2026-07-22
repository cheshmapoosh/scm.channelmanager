package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeTypes;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreObservationAttributeContributorTest {
    @Test
    void hostContributorMustNotRegisterCommonAttributes() {
        Set<String> common = streamNames(ObservationAttributeRegistry.commonOnly().all());
        Set<String> host = streamNames(new CoreObservationAttributeContributor().attributes());

        Set<String> duplicates = new LinkedHashSet<>(host);
        duplicates.retainAll(common);

        assertTrue(duplicates.isEmpty(),
                "Host contributor registers common observation attributes: " + duplicates);
    }

    @Test
    void registersTypedRoutingStepTraceAttributes() {
        ObservationAttributeRegistry registry = new ObservationAttributeRegistry(
                Set.of(new CoreObservationAttributeContributor()));

        assertEquals(ObservationAttributeTypes.KEYWORD, registry.findByName(
                ObservationStream.TRACE, CoreTraceAttributes.ROUTING_STRATEGY.name()).orElseThrow().type());
        assertEquals(ObservationAttributeTypes.LONG, registry.findByName(
                ObservationStream.TRACE, CoreTraceAttributes.ROUTING_STEP_INDEX.name()).orElseThrow().type());
        assertTrue(registry.contains(
                ObservationStream.TRACE, CoreTraceAttributes.TASK_INBOUND_ACTION.name()));
        assertTrue(registry.contains(ObservationStream.TRACE, CoreTraceAttributes.TASK_ROLE.name()));
        assertTrue(registry.contains(ObservationStream.TRACE, CoreTraceAttributes.CHAIN_DECISION.name()));
        assertTrue(registry.contains(
                ObservationStream.TRACE, CoreTraceAttributes.OPERATION_NORMALIZED_OUTCOME.name()));
    }

    private static Set<String> streamNames(Collection<ObservationAttributeKey<?>> keys) {
        return keys.stream()
                .flatMap(key -> key.streams().stream()
                        .map(stream -> stream + ":" + key.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
