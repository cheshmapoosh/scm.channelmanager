package ir.daneshrefah.scm.observation;

public interface ObservationEventSink {
    ObservationEventSignal signal();

    void write(ObservationEvent event);
}
