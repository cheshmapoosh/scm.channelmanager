package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaTraceAttributes;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UaaObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return UaaTraceAttributes.attributes();
    }
}
