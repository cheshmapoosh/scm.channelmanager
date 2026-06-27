package ir.daneshrefah.scm.web.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.web.observation.attributes.WebMetricTags;
import ir.daneshrefah.scm.web.observation.attributes.WebTraceAttributes;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

@Component
public class WebObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return Stream.concat(WebTraceAttributes.attributes().stream(), WebMetricTags.attributes().stream()).toList();
    }
}
