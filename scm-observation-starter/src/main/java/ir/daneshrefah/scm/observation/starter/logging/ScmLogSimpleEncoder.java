package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.encoder.EncoderBase;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class ScmLogSimpleEncoder extends EncoderBase<ILoggingEvent> {
    private static final byte[] EMPTY = new byte[0];
    private static final byte[] MINIMAL_FALLBACK = bytes("stream=log encoding.error=true\n");
    private static final ObservationSanitizer SANITIZER = new SecretScrubbingObservationSanitizer();

    private final ObservationSimpleLineFormatter formatter = new ObservationSimpleLineFormatter();

    @Override
    public byte[] headerBytes() {
        return EMPTY;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        if (event == null) {
            return EMPTY;
        }
        try {
            return bytes(formatter.format(ObservationStream.LOG, ScmLogEventDocumentResolver.resolve(event)));
        } catch (IOException | RuntimeException exception) {
            addError("Failed to build an SCM LOG observation document for simple output.", exception);
            return fallback(event);
        }
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY;
    }

    private byte[] fallback(ILoggingEvent event) {
        try {
            StringBuilder line = new StringBuilder(256);
            appendQuoted(line, "timestamp", timestamp(event));
            appendRaw(line, "stream", "log");
            appendRaw(line, "level", level(event));
            appendQuoted(line, "logger", sanitized("log.logger", logger(event), "application"));
            appendQuoted(line, "thread", sanitized("process.thread.name", thread(event), "unknown"));
            appendQuoted(line, "message", sanitized("message", formattedMessage(event), ""));
            appendThrowable(line, throwableProxy(event));
            appendRaw(line, "encoding.error", "true");
            return bytes(line.append('\n').toString());
        } catch (RuntimeException ignored) {
            return MINIMAL_FALLBACK;
        }
    }

    private String timestamp(ILoggingEvent event) {
        try {
            return Instant.ofEpochMilli(event.getTimeStamp()).toString();
        } catch (RuntimeException ignored) {
            return "unknown";
        }
    }

    private String level(ILoggingEvent event) {
        try {
            return event.getLevel() == null ? "INFO" : event.getLevel().toString();
        } catch (RuntimeException ignored) {
            return "INFO";
        }
    }

    private String logger(ILoggingEvent event) {
        try {
            return event.getLoggerName();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String thread(ILoggingEvent event) {
        try {
            return event.getThreadName();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String formattedMessage(ILoggingEvent event) {
        try {
            return event.getFormattedMessage();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private IThrowableProxy throwableProxy(ILoggingEvent event) {
        try {
            return event.getThrowableProxy();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void appendThrowable(StringBuilder line, IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return;
        }
        String type = throwableType(throwableProxy);
        if (type != null && !type.isBlank()) {
            appendQuoted(line, "error.type", sanitized("error.type", type, "unknown"));
        }
        String message = throwableMessage(throwableProxy);
        if (message != null && !message.isBlank()) {
            appendQuoted(line, "error.message", sanitized("error.message", message, ""));
        }
    }

    private String throwableType(IThrowableProxy throwableProxy) {
        try {
            return throwableProxy.getClassName();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String throwableMessage(IThrowableProxy throwableProxy) {
        try {
            return throwableProxy.getMessage();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String sanitized(String fieldName, String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            Object sanitized = SANITIZER.sanitize(fieldName, value);
            return sanitized instanceof String text ? text : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private void appendRaw(StringBuilder line, String fieldName, String value) {
        appendSeparator(line);
        line.append(fieldName).append('=').append(value);
    }

    private void appendQuoted(StringBuilder line, String fieldName, String value) {
        appendSeparator(line);
        line.append(fieldName).append("=\"");
        appendEscaped(line, value);
        line.append('"');
    }

    private void appendSeparator(StringBuilder line) {
        if (!line.isEmpty()) {
            line.append(' ');
        }
    }

    private void appendEscaped(StringBuilder line, String value) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\r' -> line.append("\\r");
                case '\n' -> line.append("\\n");
                case '\t' -> line.append("\\t");
                case '"' -> line.append("\\\"");
                case '\\' -> line.append("\\\\");
                default -> {
                    if (Character.isISOControl(character)) {
                        appendUnicodeEscape(line, character);
                    } else {
                        line.append(character);
                    }
                }
            }
        }
    }

    private void appendUnicodeEscape(StringBuilder line, char character) {
        line.append("\\u");
        for (int shift = 12; shift >= 0; shift -= 4) {
            line.append(Character.forDigit((character >> shift) & 0x0f, 16));
        }
    }

    private static byte[] bytes(String line) {
        return line.getBytes(StandardCharsets.UTF_8);
    }
}
