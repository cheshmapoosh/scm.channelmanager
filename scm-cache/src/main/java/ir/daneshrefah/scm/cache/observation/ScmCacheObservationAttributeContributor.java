package ir.daneshrefah.scm.cache.observation;

import ir.daneshrefah.scm.cache.observation.attributes.CacheAuditAttributes;
import ir.daneshrefah.scm.cache.observation.attributes.CacheLogAttributes;
import ir.daneshrefah.scm.cache.observation.attributes.CacheMetricTags;
import ir.daneshrefah.scm.cache.observation.attributes.CacheTraceAttributes;
import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

@Component
public class ScmCacheObservationAttributeContributor implements ObservationAttributeContributor {
    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return Stream.of(
                        CacheLogAttributes.attributes(),
                        CacheTraceAttributes.attributes(),
                        CacheAuditAttributes.attributes(),
                        CacheMetricTags.attributes()
                )
                .flatMap(Collection::stream)
                .toList();
    }
}
