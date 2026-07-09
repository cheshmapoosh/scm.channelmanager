package ir.daneshrefah.scm.observation.starter.element;

public enum ScmElementRiskLevel {
    NORMAL(0),
    WARNING(1),
    CRITICAL(2);

    private final int code;

    ScmElementRiskLevel(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
