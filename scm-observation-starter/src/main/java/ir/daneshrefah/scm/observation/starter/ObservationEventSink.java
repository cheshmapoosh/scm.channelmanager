package ir.daneshrefah.scm.observation.starter;

public interface ObservationEventSink {
    ObservationEventSignal signal();

    void write(ObservationEvent event);
}
