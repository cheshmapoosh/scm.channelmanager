package ir.daneshrefah.scm.observation.starter.element;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ScmHealthElementSample(
        ScmHealthElementId id,
        boolean materialized,
        Double capacityRatio,
        Map<String, Object> details
) {
    public ScmHealthElementSample {
        if (id == null) {
            throw new IllegalArgumentException("SCM health element sample id is required");
        }
        details = immutableDetails(details);
    }

    public ScmHealthElementSample(ScmHealthElementId id, boolean materialized, Double capacityRatio) {
        this(id, materialized, capacityRatio, Map.of());
    }

    private static Map<String, Object> immutableDetails(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
