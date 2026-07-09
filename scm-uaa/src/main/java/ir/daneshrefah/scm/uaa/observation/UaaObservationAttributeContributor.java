package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaLogAttributes;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaTraceAttributes;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

@Component
public class UaaObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return Stream.concat(UaaLogAttributes.attributes().stream(), UaaTraceAttributes.attributes().stream()).toList();
    }
}
