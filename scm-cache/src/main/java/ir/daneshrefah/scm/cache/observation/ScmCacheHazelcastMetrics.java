package ir.daneshrefah.scm.cache.observation;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastBootstrapState;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastElementType;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ScmCacheHazelcastMetrics implements MeterBinder {
    private static final String SERVICE_NAME = "scm-cache";
    private static final String SERVICE_TAG = "service";
    private static final String ELEMENT_TYPE_TAG = "element.type";

    private final ObjectProvider<HazelcastInstance> hazelcastInstanceProvider;
    private final HazelcastBootstrapState bootstrapState;

    public ScmCacheHazelcastMetrics(
            ObjectProvider<HazelcastInstance> hazelcastInstanceProvider,
            HazelcastBootstrapState bootstrapState
    ) {
        this.hazelcastInstanceProvider = hazelcastInstanceProvider;
        this.bootstrapState = bootstrapState;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("scm.cache.hazelcast.cluster.size", this, ScmCacheHazelcastMetrics::clusterSize)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.member.running", this, ScmCacheHazelcastMetrics::memberRunning)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.bootstrap.completed", bootstrapState, state -> state.bootstrapCompleted() ? 1 : 0)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.elements.registered", bootstrapState, HazelcastBootstrapState::registeredElementCount)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .register(registry);

        Gauge.builder("scm.cache.hazelcast.elements.materialized", bootstrapState, HazelcastBootstrapState::materializedElementCount)
                .tag(SERVICE_TAG, SERVICE_NAME)
                .register(registry);

        for (HazelcastElementType type : HazelcastElementType.values()) {
            Gauge.builder(
                            "scm.cache.hazelcast.elements.registered.by.type",
                            bootstrapState,
                            state -> state.registeredElementCount(type)
                    )
                    .tag(SERVICE_TAG, SERVICE_NAME)
                    .tag(ELEMENT_TYPE_TAG, type.name())
                    .register(registry);

            Gauge.builder(
                            "scm.cache.hazelcast.elements.materialized.by.type",
                            bootstrapState,
                            state -> state.materializedElementCount(type)
                    )
                    .tag(SERVICE_TAG, SERVICE_NAME)
                    .tag(ELEMENT_TYPE_TAG, type.name())
                    .register(registry);
        }
    }

    private double clusterSize() {
        HazelcastInstance instance = hazelcastInstanceProvider.getIfAvailable();
        if (!isMemberRunning(instance)) {
            return 0;
        }
        try {
            return instance.getCluster().getMembers().size();
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    private double memberRunning() {
        HazelcastInstance instance = hazelcastInstanceProvider.getIfAvailable();
        return isMemberRunning(instance) ? 1 : 0;
    }

    private boolean isMemberRunning(HazelcastInstance instance) {
        return instance != null
                && instance.getLifecycleService() != null
                && instance.getLifecycleService().isRunning();
    }
}
