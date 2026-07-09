package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static Set<String> streamNames(Collection<ObservationAttributeKey<?>> keys) {
        return keys.stream()
                .flatMap(key -> key.streams().stream()
                        .map(stream -> stream + ":" + key.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
