package ir.daneshrefah.scm.observation;

public class NoopObservationEventSink implements ObservationEventSink {
    private final ObservationEventSignal signal;

    public NoopObservationEventSink(ObservationEventSignal signal) {
        this.signal = signal;
    }

    @Override
    public ObservationEventSignal signal() {
        return signal;
    }

    @Override
    public void write(ObservationEvent event) {
        // Intentionally no-op when the corresponding signal is disabled.
    }
}
