package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.logback.ScmObservationMarkers;
import org.slf4j.Marker;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScmObservationSimpleEncoder extends EncoderBase<ILoggingEvent> {
    private static final byte[] EMPTY = new byte[0];
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String, Object>> DOCUMENT_TYPE = new TypeReference<>() {
    };

    private final ObservationSimpleLineFormatter formatter = new ObservationSimpleLineFormatter();

    @Override
    public byte[] headerBytes() {
        return EMPTY;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        ObservationStream stream = stream(event);
        if (stream == null) {
            return EMPTY;
        }
        try {
            String payload = event.getFormattedMessage();
            if (payload == null || payload.isBlank()) {
                throw new IllegalArgumentException("Observation payload is blank.");
            }
            Map<String, Object> document = OBJECT_MAPPER.readValue(payload, DOCUMENT_TYPE);
            if (document == null) {
                throw new IllegalArgumentException("Observation payload is not an object.");
            }
            return bytes(formatter.format(stream, document));
        } catch (RuntimeException | java.io.IOException exception) {
            addError("Failed to parse an SCM " + stream.value() + " observation payload for simple output.", exception);
            return bytes(formatter.format(stream, Map.of("message", "Malformed SCM observation payload")));
        }
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY;
    }

    private ObservationStream stream(ILoggingEvent event) {
        if (event == null) {
            return null;
        }
        boolean trace = hasMarker(event, ScmObservationMarkers.TRACE_MARKER_NAME);
        boolean audit = hasMarker(event, ScmObservationMarkers.AUDIT_MARKER_NAME);
        if (trace == audit) {
            return null;
        }
        return trace ? ObservationStream.TRACE : ObservationStream.AUDIT;
    }

    private boolean hasMarker(ILoggingEvent event, String markerName) {
        List<Marker> markers = event.getMarkerList();
        if (markers == null || markers.isEmpty()) {
            return false;
        }
        for (Marker marker : markers) {
            if (marker != null && (markerName.equals(marker.getName()) || marker.contains(markerName))) {
                return true;
            }
        }
        return false;
    }

    private byte[] bytes(String line) {
        return line.getBytes(StandardCharsets.UTF_8);
    }
}
