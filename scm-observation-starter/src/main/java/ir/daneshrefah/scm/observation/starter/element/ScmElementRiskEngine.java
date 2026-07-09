package ir.daneshrefah.scm.observation.starter.element;

public class ScmElementRiskEngine {
    public ScmElementRiskLevel evaluate(Double capacityRatio, ScmElementRiskPolicy policy) {
        validate(policy);
        if (capacityRatio == null) {
            return ScmElementRiskLevel.NORMAL;
        }
        validateRatio("capacityRatio", capacityRatio);
        if (capacityRatio >= policy.criticalRatio()) {
            return ScmElementRiskLevel.CRITICAL;
        }
        if (capacityRatio >= policy.warningRatio()) {
            return ScmElementRiskLevel.WARNING;
        }
        return ScmElementRiskLevel.NORMAL;
    }

    public void validate(ScmElementRiskPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("SCM element risk policy is required");
        }
        validateRatio("warningRatio", policy.warningRatio());
        validateRatio("criticalRatio", policy.criticalRatio());
        if (policy.warningRatio() >= policy.criticalRatio()) {
            throw new IllegalArgumentException(
                    "SCM element risk policy requires 0 <= warningRatio < criticalRatio <= 1"
            );
        }
    }

    private void validateRatio(String name, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException("SCM element " + name + " must be between 0 and 1");
        }
    }
}
