package ir.daneshrefah.scm.observation.element;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScmElementHealthEngine {
    private final ScmElementRiskEngine riskEngine;

    public ScmElementHealthEngine() {
        this(new ScmElementRiskEngine());
    }

    public ScmElementHealthEngine(ScmElementRiskEngine riskEngine) {
        this.riskEngine = riskEngine == null ? new ScmElementRiskEngine() : riskEngine;
    }

    public ScmElementHealthResult evaluate(ScmHealthElementSample sample, ScmElementRiskPolicy policy) {
        if (sample == null) {
            throw new IllegalArgumentException("SCM health element sample is required");
        }
        Map<String, Object> details = new LinkedHashMap<>(sample.details());
        details.put("materialized", sample.materialized());
        if (sample.capacityRatio() != null) {
            details.put("capacityRatio", sample.capacityRatio());
        }
        if (!sample.materialized()) {
            return new ScmElementHealthResult(
                    sample.id(),
                    false,
                    ScmElementRiskLevel.CRITICAL,
                    "element_not_materialized",
                    details
            );
        }
        ScmElementRiskLevel risk = riskEngine.evaluate(sample.capacityRatio(), policy);
        return new ScmElementHealthResult(
                sample.id(),
                risk != ScmElementRiskLevel.CRITICAL,
                risk,
                reason(risk, sample.capacityRatio()),
                details
        );
    }

    private String reason(ScmElementRiskLevel risk, Double capacityRatio) {
        if (capacityRatio == null || risk == ScmElementRiskLevel.NORMAL) {
            return "normal";
        }
        return risk == ScmElementRiskLevel.WARNING
                ? "capacity_ratio_warning"
                : "capacity_ratio_critical";
    }
}
