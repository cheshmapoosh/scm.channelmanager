package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ShetabObservationAttributeContributor implements ShetabProviderTraceAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return ShetabTraceAttributes.attributes();
    }
}
