package ir.daneshrefah.scm.observation.element;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScmElementRiskProperties {
    private static final double FALLBACK_WARNING_RATIO = 0.80D;
    private static final double FALLBACK_CRITICAL_RATIO = 0.90D;

    private Double defaultWarningRatio = FALLBACK_WARNING_RATIO;
    private Double defaultCriticalRatio = FALLBACK_CRITICAL_RATIO;
    private Map<String, ScmElementRiskOverride> elements = new LinkedHashMap<>();

    public ScmElementRiskPolicy resolvePolicy(String elementName) {
        ScmElementRiskOverride override = elementOverride(elementName);
        double warningRatio = ratioOrDefault(
                override == null ? null : override.getWarningRatio(),
                ratioOrDefault(defaultWarningRatio, FALLBACK_WARNING_RATIO)
        );
        double criticalRatio = ratioOrDefault(
                override == null ? null : override.getCriticalRatio(),
                ratioOrDefault(defaultCriticalRatio, FALLBACK_CRITICAL_RATIO)
        );
        return new ScmElementRiskPolicy(warningRatio, criticalRatio);
    }

    public Double getDefaultWarningRatio() {
        return defaultWarningRatio;
    }

    public void setDefaultWarningRatio(Double defaultWarningRatio) {
        this.defaultWarningRatio = defaultWarningRatio;
    }

    public Double getDefaultCriticalRatio() {
        return defaultCriticalRatio;
    }

    public void setDefaultCriticalRatio(Double defaultCriticalRatio) {
        this.defaultCriticalRatio = defaultCriticalRatio;
    }

    public Map<String, ScmElementRiskOverride> getElements() {
        return elements;
    }

    public void setElements(Map<String, ScmElementRiskOverride> elements) {
        this.elements = elements == null ? new LinkedHashMap<>() : new LinkedHashMap<>(elements);
    }

    private ScmElementRiskOverride elementOverride(String elementName) {
        if (elements == null || elements.isEmpty() || elementName == null) {
            return null;
        }
        ScmElementRiskOverride override = elements.get(elementName);
        if (override != null) {
            return override;
        }
        String normalizedName = elementName.trim();
        return normalizedName.isEmpty() ? null : elements.get(normalizedName);
    }

    private double ratioOrDefault(Double value, double defaultValue) {
        return value == null ? defaultValue : value;
    }
}
