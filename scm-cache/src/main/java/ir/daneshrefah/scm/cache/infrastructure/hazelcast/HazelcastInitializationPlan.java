package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record HazelcastInitializationPlan(
        List<HazelcastElementDefinition> elements
) {
    public HazelcastInitializationPlan {
        elements = elements == null ? List.of() : List.copyOf(elements);
    }

    public int elementCount() {
        return elements.size();
    }

    public Map<String, Integer> summarizeByType() {
        Map<HazelcastElementType, Integer> counts = new EnumMap<>(HazelcastElementType.class);
        for (HazelcastElementDefinition element : elements) {
            counts.merge(element.type(), 1, Integer::sum);
        }

        Map<String, Integer> summary = new LinkedHashMap<>();
        for (HazelcastElementType type : HazelcastElementType.values()) {
            Integer count = counts.get(type);
            if (count != null) {
                summary.put(type.name(), count);
            }
        }
        return Collections.unmodifiableMap(summary);
    }
}
