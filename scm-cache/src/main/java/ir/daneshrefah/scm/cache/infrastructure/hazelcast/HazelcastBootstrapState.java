package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class HazelcastBootstrapState {
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 300;

    private final SafeFailureMessageFormatter failureMessageFormatter;
    private final AtomicReference<Snapshot> state =
            new AtomicReference<>(Snapshot.empty());

    public Snapshot snapshot() {
        return state.get();
    }

    public void markBootstrapStarted() {
        state.set(new Snapshot(
                true,
                false,
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                null,
                null,
                Instant.now()
        ));
    }

    public void markRegistered(HazelcastInitializationPlan plan) {
        state.updateAndGet(current -> new Snapshot(
                current.bootstrapStarted(),
                false,
                countByType(plan),
                current.materializedElements(),
                elements(plan),
                current.materializedElementDefinitions(),
                null,
                null,
                Instant.now()
        ));
    }

    public void markMaterialized(HazelcastInitializationPlan plan) {
        state.updateAndGet(current -> new Snapshot(
                current.bootstrapStarted(),
                false,
                current.registeredElements(),
                countByType(plan),
                current.registeredElementDefinitions(),
                elements(plan),
                null,
                null,
                Instant.now()
        ));
    }

    public void markBootstrapCompleted() {
        state.updateAndGet(current -> new Snapshot(
                current.bootstrapStarted(),
                true,
                current.registeredElements(),
                current.materializedElements(),
                current.registeredElementDefinitions(),
                current.materializedElementDefinitions(),
                null,
                null,
                Instant.now()
        ));
    }

    public void markFailed(Throwable throwable) {
        state.updateAndGet(current -> new Snapshot(
                current.bootstrapStarted(),
                false,
                current.registeredElements(),
                current.materializedElements(),
                current.registeredElementDefinitions(),
                current.materializedElementDefinitions(),
                throwable == null ? null : throwable.getClass().getName(),
                failureMessageFormatter.format(throwable, MAX_FAILURE_MESSAGE_LENGTH),
                Instant.now()
        ));
    }

    public int registeredElementCount() {
        return snapshot().registeredElementCount();
    }

    public int registeredElementCount(HazelcastElementType type) {
        return snapshot().registeredElementCount(type);
    }

    public int materializedElementCount() {
        return snapshot().materializedElementCount();
    }

    public int materializedElementCount(HazelcastElementType type) {
        return snapshot().materializedElementCount(type);
    }

    public boolean bootstrapCompleted() {
        return snapshot().bootstrapCompleted();
    }

    private Map<HazelcastElementType, Integer> countByType(HazelcastInitializationPlan plan) {
        if (plan == null || plan.elements().isEmpty()) {
            return Map.of();
        }
        Map<HazelcastElementType, Integer> counts = new EnumMap<>(HazelcastElementType.class);
        for (HazelcastElementDefinition element : plan.elements()) {
            counts.merge(element.type(), 1, Integer::sum);
        }
        return Collections.unmodifiableMap(new EnumMap<>(counts));
    }

    private List<HazelcastElementDefinition> elements(HazelcastInitializationPlan plan) {
        return plan == null ? List.of() : List.copyOf(plan.elements());
    }

    public record Snapshot(
            boolean bootstrapStarted,
            boolean bootstrapCompleted,
            Map<HazelcastElementType, Integer> registeredElements,
            Map<HazelcastElementType, Integer> materializedElements,
            List<HazelcastElementDefinition> registeredElementDefinitions,
            List<HazelcastElementDefinition> materializedElementDefinitions,
            String lastFailureType,
            String lastFailureMessage,
            Instant lastUpdatedAt
    ) {
        public Snapshot {
            registeredElements = immutableCounts(registeredElements);
            materializedElements = immutableCounts(materializedElements);
            registeredElementDefinitions = immutableElements(registeredElementDefinitions);
            materializedElementDefinitions = immutableElements(materializedElementDefinitions);
            lastUpdatedAt = lastUpdatedAt == null ? Instant.EPOCH : lastUpdatedAt;
        }

        static Snapshot empty() {
            return new Snapshot(
                    false,
                    false,
                    Map.of(),
                    Map.of(),
                    List.of(),
                    List.of(),
                    null,
                    null,
                    Instant.EPOCH
            );
        }

        public int registeredElementCount() {
            return count(registeredElements);
        }

        public int registeredElementCount(HazelcastElementType type) {
            return type == null ? 0 : registeredElements.getOrDefault(type, 0);
        }

        public int materializedElementCount() {
            return count(materializedElements);
        }

        public int materializedElementCount(HazelcastElementType type) {
            return type == null ? 0 : materializedElements.getOrDefault(type, 0);
        }

        private static int count(Map<HazelcastElementType, Integer> elements) {
            int total = 0;
            for (Integer value : elements.values()) {
                total += value == null ? 0 : value;
            }
            return total;
        }

        private static Map<HazelcastElementType, Integer> immutableCounts(
                Map<HazelcastElementType, Integer> source
        ) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }
            return Collections.unmodifiableMap(new EnumMap<>(source));
        }

        private static List<HazelcastElementDefinition> immutableElements(
                List<HazelcastElementDefinition> source
        ) {
            return source == null || source.isEmpty() ? List.of() : List.copyOf(source);
        }
    }
}
