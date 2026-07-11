package ir.daneshrefah.scm.observation.starter.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ObservationSimpleLineFormatter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private static final ObservationSanitizer SANITIZER = new SecretScrubbingObservationSanitizer();

    public String format(ObservationStream stream, Map<String, ?> document) {
        if (stream == null) {
            return "";
        }

        Map<String, ?> safeDocument = document == null ? Map.of() : document;
        StringBuilder line = new StringBuilder(256);
        Set<String> consumed = new HashSet<>();

        appendPreferred(line, safeDocument, consumed, "timestamp", "@timestamp");
        appendRaw(line, "stream", stream.value());
        consumed.add("stream");
        consumed.add("event.stream");
        appendPreferred(line, safeDocument, consumed, "level", "log.level");
        appendPreferred(line, safeDocument, consumed, "service.name", "service.name");
        appendPreferred(line, safeDocument, consumed, "deployment.environment", "deployment.environment");
        appendPreferred(line, safeDocument, consumed, "trace.id", "trace.id");
        appendPreferred(line, safeDocument, consumed, "span.id", "span.id");
        appendPreferred(line, safeDocument, consumed, "correlation.id", "correlation.id");
        appendPreferred(line, safeDocument, consumed, "logger", "log.logger");
        appendPreferred(line, safeDocument, consumed, "thread", "process.thread.name");
        appendPreferred(line, safeDocument, consumed, "message", "message");

        TreeSet<String> remaining = new TreeSet<>();
        for (String fieldName : safeDocument.keySet()) {
            if (fieldName != null && !consumed.contains(fieldName)) {
                remaining.add(fieldName);
            }
        }
        for (String fieldName : remaining) {
            appendValue(line, fieldName, fieldName, safeDocument.get(fieldName));
        }
        return line.append('\n').toString();
    }

    private void appendPreferred(
            StringBuilder line,
            Map<String, ?> document,
            Set<String> consumed,
            String outputName,
            String documentName
    ) {
        consumed.add(outputName);
        consumed.add(documentName);
        appendValue(line, outputName, documentName, document.get(documentName));
    }

    private void appendValue(StringBuilder line, String outputName, String documentName, Object value) {
        Object sanitized = SANITIZER.sanitize(documentName, value);
        if (sanitized == null) {
            return;
        }
        appendSeparator(line);
        line.append(outputName).append('=').append(render(sanitized));
    }

    private void appendRaw(StringBuilder line, String fieldName, String value) {
        appendSeparator(line);
        line.append(fieldName).append('=').append(value);
    }

    private void appendSeparator(StringBuilder line) {
        if (!line.isEmpty()) {
            line.append(' ');
        }
    }

    private String render(Object value) {
        if (value instanceof Boolean || value instanceof Number && finite((Number) value)) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> || value instanceof Iterable<?> || value.getClass().isArray()) {
            try {
                return OBJECT_MAPPER.writeValueAsString(value);
            } catch (JsonProcessingException ignored) {
                return quote(String.valueOf(value));
            }
        }
        return quote(String.valueOf(value));
    }

    private boolean finite(Number number) {
        if (number instanceof Double value) {
            return Double.isFinite(value);
        }
        if (number instanceof Float value) {
            return Float.isFinite(value);
        }
        return true;
    }

    private String quote(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\r' -> escaped.append("\\r");
                case '\n' -> escaped.append("\\n");
                case '\t' -> escaped.append("\\t");
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                default -> {
                    if (Character.isISOControl(character)) {
                        appendUnicodeEscape(escaped, character);
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }

    private void appendUnicodeEscape(StringBuilder escaped, char character) {
        escaped.append("\\u");
        for (int shift = 12; shift >= 0; shift -= 4) {
            escaped.append(Character.forDigit((character >> shift) & 0x0f, 16));
        }
    }
}
