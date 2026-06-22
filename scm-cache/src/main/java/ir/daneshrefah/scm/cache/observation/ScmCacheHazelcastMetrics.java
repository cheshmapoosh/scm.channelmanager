package ir.daneshrefah.scm.cache.observation;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastBootstrapState;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastElementHealthSampleProvider;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastElementRiskPolicyProvider;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastElementType;
import ir.daneshrefah.scm.observation.element.ScmElementHealthEngine;
import ir.daneshrefah.scm.observation.element.ScmElementHealthResult;
import ir.daneshrefah.scm.observation.element.ScmElementRiskLevel;
import ir.daneshrefah.scm.observation.element.ScmHealthElementId;
import ir.daneshrefah.scm.observation.element.ScmHealthElementSample;
import ir.daneshrefah.scm.observation.metrics.ScmMetricNames;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ScmCacheHazelcastMetrics implements MeterBinder {
    private static final String SERVICE_NAME = "scm-cache";
    private static final String COMPONENT_NAME = "hazelcast";
    private static final String SERVICE_TAG = "service";
    private static final String ELEMENT_TYPE_TAG = "element_type";
    private static final String COMPONENT_TAG = "component";
    private static final String ELEMENT_NAME_TAG = "element_name";

    private final ObjectProvider<HazelcastInstance> hazelcastInstanceProvider;
    private final HazelcastBootstrapState bootstrapState;
    private final HazelcastElementHealthSampleProvider sampleProvider;
    private final HazelcastElementRiskPolicyProvider riskPolicyProvider;
    private final ScmElementHealthEngine healthEngine;
    private final Set<String> registeredElementMeters = ConcurrentHashMap.newKeySet();
    private final AtomicReference<ElementMetricSnapshot> latestSnapshot =
            new AtomicReference<>(ElementMetricSnapshot.empty());
    private volatile MeterRegistry meterRegistry;

    public ScmCacheHazelcastMetrics(
            ObjectProvider<HazelcastInstance> hazelcastInstanceProvider,
            HazelcastBootstrapState bootstrapState,
            HazelcastElementHealthSampleProvider sampleProvider,
            HazelcastElementRiskPolicyProvider riskPolicyProvider,
            ScmElementHealthEngine healthEngine
    ) {
        this.hazelcastInstanceProvider = hazelcastInstanceProvider;
        this.bootstrapState = bootstrapState;
        this.sampleProvider = sampleProvider;
        this.riskPolicyProvider = riskPolicyProvider;
        this.healthEngine = healthEngine;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        Gauge.builder("scm.cache.hazelcast.cluster.size", this, ScmCacheHazelcastMetrics::clusterSize)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, COMPONENT_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.member.running", this, ScmCacheHazelcastMetrics::memberRunning)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, COMPONENT_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.bootstrap.completed", bootstrapState, state -> state.bootstrapCompleted() ? 1 : 0)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, COMPONENT_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.elements.registered", bootstrapState, HazelcastBootstrapState::registeredElementCount)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, COMPONENT_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.elements.materialized", bootstrapState, HazelcastBootstrapState::materializedElementCount)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, COMPONENT_NAME)
                .register(registry);

        for (HazelcastElementType type : HazelcastElementType.values()) {
            Gauge.builder(
                            "scm.cache.hazelcast.elements.registered.by.type",
                            bootstrapState,
                            state -> state.registeredElementCount(type)
                    )
                    .tag(SERVICE_TAG, SERVICE_NAME)
                    .tag(COMPONENT_TAG, COMPONENT_NAME)
                    .tag(ELEMENT_TYPE_TAG, type.name())
                    .register(registry);

            Gauge.builder(
                            "scm.cache.hazelcast.elements.materialized.by.type",
                            bootstrapState,
                            state -> state.materializedElementCount(type)
                    )
                    .tag(SERVICE_TAG, SERVICE_NAME)
                    .tag(COMPONENT_TAG, COMPONENT_NAME)
                    .tag(ELEMENT_TYPE_TAG, type.name())
                    .register(registry);
        }
        refreshElementMeters();
    }

    @Scheduled(fixedDelay = 15000L, initialDelay = 1000L)
    public void refreshElementMeters() {
        MeterRegistry registry = meterRegistry;
        if (registry == null) {
            return;
        }
        HazelcastInstance instance = hazelcastInstanceProvider.getIfAvailable();
        boolean memberRunning = isMemberRunning(instance);
        int clusterSize = memberRunning ? clusterSize(instance) : 0;
        Map<ScmHealthElementId, ScmHealthElementSample> sampleSnapshot = new LinkedHashMap<>();
        Map<ScmHealthElementId, ScmElementHealthResult> resultSnapshot = new LinkedHashMap<>();
        for (ScmHealthElementSample sample : sampleProvider.samples()) {
            ScmElementHealthResult result = healthEngine.evaluate(
                    sample,
                    riskPolicyProvider.policyFor(sample.id().name())
            );
            sampleSnapshot.put(sample.id(), sample);
            resultSnapshot.put(sample.id(), result);
        }
        Map<ScmHealthElementId, ScmHealthElementSample> immutableSamples =
                Collections.unmodifiableMap(sampleSnapshot);
        Map<ScmHealthElementId, ScmElementHealthResult> immutableResults =
                Collections.unmodifiableMap(resultSnapshot);
        latestSnapshot.set(new ElementMetricSnapshot(memberRunning, clusterSize, immutableSamples, immutableResults));
        for (ScmHealthElementId id : immutableSamples.keySet()) {
            registerElementMeters(registry, id);
        }
    }

    private void registerElementMeters(MeterRegistry registry, ScmHealthElementId id) {
        String meterKey = id.component() + "|" + id.type() + "|" + id.name();
        if (!registeredElementMeters.add(meterKey)) {
            return;
        }
        Gauge.builder(ScmMetricNames.ELEMENT_HEALTH, this, metrics -> metrics.elementHealth(id))
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, id.component())
                .tag(ELEMENT_TYPE_TAG, id.type())
                .tag(ELEMENT_NAME_TAG, id.name())
                .register(registry);
        Gauge.builder(ScmMetricNames.ELEMENT_RISK, this, metrics -> metrics.elementRisk(id))
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, id.component())
                .tag(ELEMENT_TYPE_TAG, id.type())
                .tag(ELEMENT_NAME_TAG, id.name())
                .register(registry);
        Gauge.builder(ScmMetricNames.ELEMENT_MATERIALIZED, this, metrics -> metrics.elementMaterialized(id))
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, id.component())
                .tag(ELEMENT_TYPE_TAG, id.type())
                .tag(ELEMENT_NAME_TAG, id.name())
                .register(registry);
        Gauge.builder(ScmMetricNames.ELEMENT_CAPACITY_RATIO, this, metrics -> metrics.elementCapacityRatio(id))
                .tag(SERVICE_TAG, SERVICE_NAME)
                .tag(COMPONENT_TAG, id.component())
                .tag(ELEMENT_TYPE_TAG, id.type())
                .tag(ELEMENT_NAME_TAG, id.name())
                .register(registry);
    }

    private double elementHealth(ScmHealthElementId id) {
        ScmElementHealthResult result = latestSnapshot.get().results().get(id);
        return result != null && result.healthy() ? 1.0D : 0.0D;
    }

    private double elementRisk(ScmHealthElementId id) {
        ScmElementHealthResult result = latestSnapshot.get().results().get(id);
        return result == null ? ScmElementRiskLevel.CRITICAL.code() : result.risk().code();
    }

    private double elementMaterialized(ScmHealthElementId id) {
        ScmHealthElementSample sample = latestSnapshot.get().samples().get(id);
        return sample != null && sample.materialized() ? 1.0D : 0.0D;
    }

    private double elementCapacityRatio(ScmHealthElementId id) {
        ScmHealthElementSample sample = latestSnapshot.get().samples().get(id);
        return sample == null || sample.capacityRatio() == null ? Double.NaN : sample.capacityRatio();
    }

    private double clusterSize() {
        return latestSnapshot.get().clusterSize();
    }

    private double memberRunning() {
        return latestSnapshot.get().memberRunning() ? 1.0D : 0.0D;
    }

    private boolean isMemberRunning(HazelcastInstance instance) {
        return instance != null
                && instance.getLifecycleService() != null
                && instance.getLifecycleService().isRunning();
    }

    private int clusterSize(HazelcastInstance instance) {
        try {
            return instance.getCluster().getMembers().size();
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    private record ElementMetricSnapshot(
            boolean memberRunning,
            int clusterSize,
            Map<ScmHealthElementId, ScmHealthElementSample> samples,
            Map<ScmHealthElementId, ScmElementHealthResult> results
    ) {
        static ElementMetricSnapshot empty() {
            return new ElementMetricSnapshot(false, 0, Map.of(), Map.of());
        }
    }
}
