package ir.daneshrefah.scm.observation.starter.logback;

import ir.daneshrefah.scm.observation.starter.ObservationDocumentSerializer;
import ir.daneshrefah.scm.observation.starter.ObservationEvent;
import ir.daneshrefah.scm.observation.starter.ObservationEventSignal;
import ir.daneshrefah.scm.observation.starter.ObservationEventSink;

public class LogbackTraceObservationEventSink implements ObservationEventSink {
    private final ObservationDocumentSerializer serializer;
    private final LogbackObservationEventPublisher publisher;

    public LogbackTraceObservationEventSink(
            ObservationDocumentSerializer serializer,
            LogbackObservationEventPublisher publisher
    ) {
        this.serializer = serializer;
        this.publisher = publisher;
    }

    @Override
    public ObservationEventSignal signal() {
        return ObservationEventSignal.TRACE;
    }

    @Override
    public void write(ObservationEvent event) {
        if (event == null || event.signal() != ObservationEventSignal.TRACE) {
            return;
        }
        publisher.publish(event.signal(), event.sourceClass(), serializer.serialize(event.document()));
    }
}
