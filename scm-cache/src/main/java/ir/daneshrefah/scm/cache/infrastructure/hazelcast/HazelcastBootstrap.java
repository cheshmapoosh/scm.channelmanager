package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;
import ir.daneshrefah.scm.cache.observation.ScmCacheInitLogging;
import ir.daneshrefah.scm.cache.observation.ScmCacheLogFields;
import ir.daneshrefah.scm.cache.observation.ScmCacheObservationEvents;
import ir.daneshrefah.scm.cache.service.InstanceCacheConfigService;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.ObservationStream;
import ir.daneshrefah.scm.observation.logging.ScmLogMarkers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class HazelcastBootstrap {
    private static final List<String> REQUIRED_ELEMENT_LOG_FIELDS = List.of(
            ScmCacheLogFields.HAZELCAST_ELEMENT_TYPE,
            ScmCacheLogFields.HAZELCAST_ELEMENT_NAME,
            ScmCacheLogFields.HAZELCAST_ELEMENT_CONFIG
    );

    private final InstanceCacheConfigService configService;
    private final HazelcastElementRegistry elementRegistry;
    private final HazelcastBootstrapState bootstrapState;
    // Ensures scm-cache ObservationAttributeContributor is registered before bootstrap logs are emitted.
    private final ObservationAttributeRegistry observationAttributeRegistry;
    private final AtomicBoolean registryAvailabilityWarningLogged = new AtomicBoolean();

    public HazelcastInstance start(Config config) {
        bootstrapState.markBootstrapStarted();
        checkObservationRegistryAvailability();
        logInit(
                "Hazelcast bootstrap started",
                ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_STARTED,
                "unknown"
        );

        try {
            List<InstanceConfigEntity> definitions = configService.loadAll();
            logInit(
                    "Hazelcast bootstrap config loaded",
                    ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_CONFIG_LOADED,
                    "success",
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_ELEMENT_COUNT, definitions.size())
            );

            HazelcastInitializationPlan plan = elementRegistry.register(config, definitions);
            bootstrapState.markRegistered(plan);
            logInit(
                    "Hazelcast bootstrap config registered",
                    ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_CONFIG_REGISTERED,
                    "success",
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_ELEMENT_COUNT, plan.elementCount()),
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_ELEMENT_SUMMARY, formatSummary(plan.summarizeByType()))
            );
            logRegisteredElements(plan);

            HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
            logInit(
                    "Hazelcast bootstrap member started",
                    ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_MEMBER_STARTED,
                    "success",
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_CLUSTER_SIZE, clusterSize(instance)),
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_MEMBER_ADDRESS, memberAddress(instance))
            );

            elementRegistry.materialize(instance, plan);
            bootstrapState.markMaterialized(plan);
            logInit(
                    "Hazelcast bootstrap objects materialized",
                    ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_OBJECTS_MATERIALIZED,
                    "success",
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_MATERIALIZED_COUNT, plan.elementCount()),
                    ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_MATERIALIZED_SUMMARY, formatSummary(plan.summarizeByType()))
            );
            logMaterializedElements(plan);

            bootstrapState.markBootstrapCompleted();
            logInit(
                    "Hazelcast bootstrap completed",
                    ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_COMPLETED,
                    "success"
            );

            return instance;
        } catch (RuntimeException exception) {
            bootstrapState.markFailed(exception);
            logInitFailure(exception);
            throw exception;
        }
    }

    private void logRegisteredElements(HazelcastInitializationPlan plan) {
        for (HazelcastElementDefinition element : plan.elements()) {
            logInit(
                    "Hazelcast element registered",
                    ScmCacheObservationEvents.HAZELCAST_ELEMENT_REGISTERED,
                    "success",
                    elementFields(element)
            );
        }
    }

    private void logMaterializedElements(HazelcastInitializationPlan plan) {
        for (HazelcastElementDefinition element : plan.elements()) {
            logInit(
                    "Hazelcast element materialized",
                    ScmCacheObservationEvents.HAZELCAST_ELEMENT_MATERIALIZED,
                    "success",
                    elementFields(element)
            );
        }
    }

    private void checkObservationRegistryAvailability() {
        List<String> missingFields = REQUIRED_ELEMENT_LOG_FIELDS.stream()
                .filter(field -> !observationAttributeRegistry.contains(ObservationStream.LOG, field))
                .toList();
        if (!missingFields.isEmpty() && registryAvailabilityWarningLogged.compareAndSet(false, true)) {
            log.warn(
                    "SCM cache observation registry is missing Hazelcast element log attributes; JSONL element fields may be dropped: {}",
                    String.join(",", missingFields)
            );
        }
    }

    private Object[] elementFields(HazelcastElementDefinition element) {
        List<Object> fields = new ArrayList<>();
        fields.add(ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_ELEMENT_TYPE, element.type().name()));
        fields.add(ScmCacheInitLogging.kv(ScmCacheLogFields.HAZELCAST_ELEMENT_NAME, element.name()));
        addIfPresent(fields, ScmCacheLogFields.HAZELCAST_ELEMENT_CONFIG, element.configText());
        return fields.toArray();
    }

    private void addIfPresent(List<Object> fields, String name, Object value) {
        if (value != null) {
            fields.add(ScmCacheInitLogging.kv(name, value));
        }
    }

    private void logInit(String message, String action, String outcome, Object... fields) {
        log.info(
                ScmLogMarkers.SCM_EVENT,
                message,
                ScmCacheInitLogging.initArguments(action, outcome, fields)
        );
    }

    private void logInitFailure(RuntimeException exception) {
        log.error(
                ScmLogMarkers.SCM_EVENT,
                "Hazelcast bootstrap failed",
                withThrowable(
                        ScmCacheInitLogging.initArguments(
                                ScmCacheObservationEvents.HAZELCAST_BOOTSTRAP_FAILED,
                                "failure"
                        ),
                        exception
                )
        );
    }

    private Object[] withThrowable(Object[] arguments, Throwable throwable) {
        Object[] result = Arrays.copyOf(arguments, arguments.length + 1);
        result[arguments.length] = throwable;
        return result;
    }

    private String formatSummary(Map<String, Integer> summary) {
        if (summary == null || summary.isEmpty()) {
            return "none";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            if (!result.isEmpty()) {
                result.append(',');
            }
            result.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return result.toString();
    }

    private int clusterSize(HazelcastInstance instance) {
        return instance.getCluster().getMembers().size();
    }

    private String memberAddress(HazelcastInstance instance) {
        return String.valueOf(instance.getCluster().getLocalMember().getAddress());
    }
}
