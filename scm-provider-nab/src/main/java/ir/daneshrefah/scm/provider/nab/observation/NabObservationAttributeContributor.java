package ir.daneshrefah.scm.provider.nab.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.provider.nab.observation.attributes.NabMetricTags;
import ir.daneshrefah.scm.provider.nab.observation.attributes.NabTraceAttributes;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

@Component
public class NabObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return Stream.concat(NabTraceAttributes.attributes().stream(), NabMetricTags.attributes().stream()).toList();
    }
}
