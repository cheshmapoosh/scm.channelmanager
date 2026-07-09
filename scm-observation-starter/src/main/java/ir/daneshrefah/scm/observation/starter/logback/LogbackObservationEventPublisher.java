package ir.daneshrefah.scm.observation.starter.logback;

import ir.daneshrefah.scm.observation.starter.ObservationEventSignal;

public interface LogbackObservationEventPublisher {
    void publish(ObservationEventSignal signal, Class<?> sourceClass, String payload);
}
