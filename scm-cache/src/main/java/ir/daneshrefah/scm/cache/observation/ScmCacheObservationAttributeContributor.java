package ir.daneshrefah.scm.cache.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ScmCacheObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return ScmCacheObservationAttributes.attributes();
    }
}
