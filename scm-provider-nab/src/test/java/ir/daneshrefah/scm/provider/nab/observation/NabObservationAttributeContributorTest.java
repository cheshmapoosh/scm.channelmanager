package ir.daneshrefah.scm.provider.nab.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NabObservationAttributeContributorTest {
    @Test
    void hostContributorMustNotRegisterCommonAttributes() {
        Set<String> common = streamNames(ObservationAttributeRegistry.commonOnly().all());
        Set<String> host = streamNames(new NabObservationAttributeContributor().attributes());

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
