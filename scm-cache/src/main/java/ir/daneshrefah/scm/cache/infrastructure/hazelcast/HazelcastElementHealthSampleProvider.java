package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import ir.daneshrefah.scm.observation.element.ScmHealthElementId;
import ir.daneshrefah.scm.observation.element.ScmHealthElementSample;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class HazelcastElementHealthSampleProvider {
    private static final String COMPONENT = "hazelcast";

    private final HazelcastBootstrapState bootstrapState;
    private final ObjectProvider<HazelcastInstance> hazelcastInstanceProvider;

    public List<ScmHealthElementSample> samples() {
        HazelcastBootstrapState.Snapshot snapshot = bootstrapState.snapshot();
        HazelcastInstance instance = hazelcastInstanceProvider.getIfAvailable();
        boolean memberRunning = isMemberRunning(instance);
        Set<String> materializedElements = elementKeys(snapshot.materializedElementDefinitions());

        return snapshot.registeredElementDefinitions().stream()
                .map(element -> sample(element, instance, memberRunning, materializedElements))
                .toList();
    }

    private ScmHealthElementSample sample(
            HazelcastElementDefinition element,
            HazelcastInstance instance,
            boolean memberRunning,
            Set<String> materializedElements
    ) {
        boolean materialized = memberRunning && materializedElements.contains(elementKey(element));
        Map<String, Object> details = new LinkedHashMap<>();
        Double capacityRatio = materialized ? capacityRatio(instance, element, details) : null;
        return new ScmHealthElementSample(
                new ScmHealthElementId(COMPONENT, element.type().name(), element.name()),
                materialized,
                capacityRatio,
                details
        );
    }

    private Double capacityRatio(
            HazelcastInstance instance,
            HazelcastElementDefinition element,
            Map<String, Object> details
    ) {
        if (instance == null || element.type() != HazelcastElementType.MAP) {
            return null;
        }
        try {
            MapConfig mapConfig = instance.getConfig().getMapConfig(element.name());
            EvictionConfig evictionConfig = mapConfig.getEvictionConfig();
            if (evictionConfig == null || !supportsEntryCapacity(evictionConfig.getMaxSizePolicy())) {
                return null;
            }
            int evictionSize = evictionConfig.getSize();
            if (evictionSize <= 0) {
                return null;
            }

            IMap<?, ?> map = instance.getMap(element.name());
            LocalMapStats stats = map.getLocalMapStats();
            long ownedEntryCount = Math.max(0L, stats.getOwnedEntryCount());
            details.put("ownedEntryCount", ownedEntryCount);
            details.put("evictionSize", evictionSize);
            details.put("evictionMaxSizePolicy", evictionConfig.getMaxSizePolicy().name());
            return boundedRatio((double) ownedEntryCount / evictionSize);
        } catch (RuntimeException exception) {
            log.debug("Unable to sample Hazelcast MAP capacity ratio for {}", element.name(), exception);
            return null;
        }
    }

    private boolean supportsEntryCapacity(MaxSizePolicy policy) {
        return policy == MaxSizePolicy.PER_NODE
                || policy == MaxSizePolicy.PER_PARTITION
                || policy == MaxSizePolicy.ENTRY_COUNT;
    }

    private double boundedRatio(double ratio) {
        if (Double.isNaN(ratio) || Double.isInfinite(ratio)) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, ratio));
    }

    private Set<String> elementKeys(List<HazelcastElementDefinition> elements) {
        Set<String> keys = new HashSet<>();
        for (HazelcastElementDefinition element : elements) {
            keys.add(elementKey(element));
        }
        return keys;
    }

    private String elementKey(HazelcastElementDefinition element) {
        return element.type().name() + ":" + element.name();
    }

    private boolean isMemberRunning(HazelcastInstance instance) {
        return instance != null
                && instance.getLifecycleService() != null
                && instance.getLifecycleService().isRunning();
    }
}
