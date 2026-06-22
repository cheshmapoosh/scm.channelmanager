package ir.daneshrefah.scm.observation.element;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ScmElementHealthResult(
        ScmHealthElementId id,
        boolean healthy,
        ScmElementRiskLevel risk,
        String reason,
        Map<String, Object> details
) {
    public ScmElementHealthResult {
        if (id == null) {
            throw new IllegalArgumentException("SCM element health result id is required");
        }
        risk = risk == null ? ScmElementRiskLevel.NORMAL : risk;
        reason = reason == null || reason.isBlank() ? "normal" : reason.trim();
        details = immutableDetails(details);
    }

    private static Map<String, Object> immutableDetails(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
