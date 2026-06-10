package ir.daneshrefah.scm.observation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ObservationEvent(
        ObservationEventSignal signal,
        Class<?> sourceClass,
        Map<String, Object> document
) {
    public ObservationEvent {
        signal = Objects.requireNonNull(signal, "signal");
        sourceClass = sourceClass == null ? ScmObservation.class : sourceClass;
        document = document == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(document));
    }
}
