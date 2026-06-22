package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.observation.ScmCacheObservationAttributes;
import ir.daneshrefah.scm.cache.observation.ScmCacheObservationEvents;
import ir.daneshrefah.scm.observation.CorrelationType;
import ir.daneshrefah.scm.observation.ScmObservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component("scmCacheHazelcast")
@Slf4j
public class ScmCacheHazelcastHealthIndicator implements HealthIndicator {
    private static final String NO_FAILURE = "none";

    private final ObjectProvider<HazelcastInstance> hazelcastInstanceProvider;
    private final ObjectProvider<ScmObservation> observationProvider;
    private final HazelcastBootstrapState bootstrapState;
    private final int minimumClusterSize;
    private final AtomicReference<Status> lastStatus = new AtomicReference<>();

    public ScmCacheHazelcastHealthIndicator(
            ObjectProvider<HazelcastInstance> hazelcastInstanceProvider,
            ObjectProvider<ScmObservation> observationProvider,
            HazelcastBootstrapState bootstrapState,
            @Value("${scm.cache.health.hazelcast.minimum-cluster-size:1}") int minimumClusterSize
    ) {
        this.hazelcastInstanceProvider = hazelcastInstanceProvider;
        this.observationProvider = observationProvider;
        this.bootstrapState = bootstrapState;
        this.minimumClusterSize = Math.max(1, minimumClusterSize);
    }

    @Override
    public Health health() {
        HazelcastBootstrapState.Snapshot snapshot = bootstrapState.snapshot();
        HazelcastInstance instance = hazelcastInstanceProvider.getIfAvailable();
        boolean memberRunning = isMemberRunning(instance);
        int clusterSize = memberRunning ? clusterSize(instance) : 0;
        int registeredCount = snapshot.registeredElementCount();
        int materializedCount = snapshot.materializedElementCount();

        Map<String, Object> details = healthDetails(
                snapshot,
                clusterSize,
                registeredCount,
                materializedCount
        );

        boolean ready = instance != null
                && memberRunning
                && snapshot.bootstrapCompleted()
                && registeredCount == materializedCount
                && clusterSize >= minimumClusterSize;

        Status status = ready ? Status.UP : Status.DOWN;
        logHealthChange(status, clusterSize, registeredCount, materializedCount);

        Health.Builder builder = ready ? Health.up() : Health.down();
        return builder.withDetails(details).build();
    }

    private Map<String, Object> healthDetails(
            HazelcastBootstrapState.Snapshot snapshot,
            int clusterSize,
            int registeredCount,
            int materializedCount
    ) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("cluster.size", clusterSize);
        details.put("bootstrap.started", snapshot.bootstrapStarted());
        details.put("bootstrap.completed", snapshot.bootstrapCompleted());
        details.put("registered.count", registeredCount);
        details.put("materialized.count", materializedCount);
        details.put("lastFailure.type", textOrDefault(snapshot.lastFailureType(), NO_FAILURE));
        details.put("lastFailure.message", textOrDefault(snapshot.lastFailureMessage(), NO_FAILURE));
        return details;
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
            log.debug("Unable to read Hazelcast cluster size for health", exception);
            return 0;
        }
    }

    private void logHealthChange(
            Status status,
            int clusterSize,
            int registeredCount,
            int materializedCount
    ) {
        Status previous = lastStatus.getAndSet(status);
        if (status.equals(previous)) {
            return;
        }

        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            return;
        }

        observation.log()
                .event()
                .loggerName(ScmCacheHazelcastHealthIndicator.class)
                .correlationType(CorrelationType.LIFECYCLE.value())
                .category(ScmCacheObservationEvents.SCM_CACHE_HEALTH)
                .action(ScmCacheObservationEvents.HAZELCAST_HEALTH_CHANGED)
                .outcome(Status.UP.equals(status) ? "success" : "failure")
                .info("Hazelcast health changed")
                .attribute(ScmCacheObservationAttributes.HAZELCAST_CLUSTER_SIZE, clusterSize)
                .attribute(ScmCacheObservationAttributes.HAZELCAST_ELEMENT_COUNT, registeredCount)
                .attribute(ScmCacheObservationAttributes.HAZELCAST_MATERIALIZED_COUNT, materializedCount)
                .write();
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
