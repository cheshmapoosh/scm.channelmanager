package ir.daneshrefah.scm.observation.logback;

import ir.daneshrefah.scm.observation.ObservationDocumentSerializer;
import ir.daneshrefah.scm.observation.ObservationEvent;
import ir.daneshrefah.scm.observation.ObservationEventSignal;
import ir.daneshrefah.scm.observation.ObservationEventSink;

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
