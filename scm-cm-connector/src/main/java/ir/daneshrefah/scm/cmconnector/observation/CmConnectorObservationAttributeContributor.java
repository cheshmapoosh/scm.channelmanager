package ir.daneshrefah.scm.cmconnector.observation;

import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorMetricTags;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorTraceAttributes;
import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

@Component
public class CmConnectorObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return Stream.concat(
                CmConnectorTraceAttributes.attributes().stream(),
                CmConnectorMetricTags.attributes().stream()
        ).toList();
    }
}
