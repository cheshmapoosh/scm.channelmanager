package ir.daneshrefah.scm.observation.logback;

import ir.daneshrefah.scm.observation.ObservationEventSignal;

public interface LogbackObservationEventPublisher {
    void publish(ObservationEventSignal signal, Class<?> sourceClass, String payload);
}
