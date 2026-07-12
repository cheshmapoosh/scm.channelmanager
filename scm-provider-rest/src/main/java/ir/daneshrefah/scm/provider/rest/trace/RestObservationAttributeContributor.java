package ir.daneshrefah.scm.provider.rest.trace;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;

import java.util.Collection;

public class RestObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return RestTraceAttributes.attributes();
    }
}
