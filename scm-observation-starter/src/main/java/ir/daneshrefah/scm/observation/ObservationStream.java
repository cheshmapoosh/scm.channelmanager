package ir.daneshrefah.scm.observation;

public enum ObservationStream {
    LOG("log"),
    TRACE("trace"),
    AUDIT("audit"),
    METRIC("metric");

    private final String value;

    ObservationStream(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
