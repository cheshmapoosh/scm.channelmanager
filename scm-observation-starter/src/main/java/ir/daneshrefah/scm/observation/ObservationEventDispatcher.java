package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ObservationEventDispatcher {
    private final ObservationSignalPolicy signalPolicy;
    private final Map<ObservationEventSignal, ObservationEventSink> sinks;

    public ObservationEventDispatcher(
            ObservationSignalPolicy signalPolicy,
            List<ObservationEventSink> sinks
    ) {
        this.signalPolicy = signalPolicy;
        this.sinks = sinksBySignal(sinks);
    }

    public void write(ObservationEvent event) {
        if (event == null || !enabled(event.signal())) {
            return;
        }
        ObservationEventSink sink = sinks.get(event.signal());
        if (sink != null) {
            sink.write(event);
        }
    }

    private boolean enabled(ObservationEventSignal signal) {
        if (signalPolicy == null || signal == null) {
            return false;
        }
        return switch (signal) {
            case TRACE -> signalPolicy.isEnabled(ObservationSignal.TRACE);
            case AUDIT -> signalPolicy.isEnabled(ObservationSignal.AUDIT);
        };
    }

    private Map<ObservationEventSignal, ObservationEventSink> sinksBySignal(List<ObservationEventSink> configuredSinks) {
        Map<ObservationEventSignal, ObservationEventSink> selected = new EnumMap<>(ObservationEventSignal.class);
        if (configuredSinks == null) {
            return selected;
        }
        for (ObservationEventSink sink : configuredSinks) {
            if (sink == null || sink.signal() == null) {
                continue;
            }
            ObservationEventSink previous = selected.putIfAbsent(sink.signal(), sink);
            if (previous != null) {
                throw new IllegalStateException("Duplicate SCM observation sink for signal " + sink.signal());
            }
        }
        return selected;
    }
}
