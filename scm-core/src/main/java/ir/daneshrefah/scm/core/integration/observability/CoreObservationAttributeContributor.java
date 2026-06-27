package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.core.integration.observability.attributes.CoreLogAttributes;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreMetricTags;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

@Component
public class CoreObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return Stream.of(
                        CoreLogAttributes.attributes(),
                        CoreTraceAttributes.attributes(),
                        CoreMetricTags.attributes()
                )
                .flatMap(Collection::stream)
                .toList();
    }
}
