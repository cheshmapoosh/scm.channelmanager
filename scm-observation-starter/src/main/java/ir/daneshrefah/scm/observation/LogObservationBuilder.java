package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.ScmCommonLogAttributes;
import ir.daneshrefah.scm.observation.logging.ScmLogMarkers;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class LogObservationBuilder extends AbstractObservationBuilder<LogObservationBuilder> {
    private static final int MAX_ERROR_MESSAGE_LENGTH = 300;
    private static final Pattern SENSITIVE_ASSIGNMENT = Pattern.compile(
            "(?i)(password|token|authorization|client_secret|authorization_code|pin|cvv2?|pan|account[_ -]?number)\\s*[:=]\\s*\\S+"
    );

    private String level = "INFO";
    private String loggerName = "application";
    private String threadName;
    private String message = "";
    private String category;
    private String correlationType;
    private ObservationRecordKind recordKind = ObservationRecordKind.PLAIN;
    private Throwable throwable;

    LogObservationBuilder(ScmObservation observation) {
        super(observation);
        this.action = "log.event";
    }

    public LogObservationBuilder plain() {
        this.recordKind = ObservationRecordKind.PLAIN;
        return this;
    }

    public LogObservationBuilder context() {
        this.recordKind = ObservationRecordKind.CONTEXT;
        this.category = "scm.context";
        this.action = "runtime.context";
        this.correlationType = "lifecycle";
        return this;
    }

    public LogObservationBuilder event() {
        this.recordKind = ObservationRecordKind.EVENT;
        this.category = "application";
        return this;
    }

    public LogObservationBuilder exception(Throwable throwable) {
        if (this.recordKind == ObservationRecordKind.PLAIN) {
            this.recordKind = ObservationRecordKind.EXCEPTION;
        }
        this.throwable = throwable;
        if (throwable != null) {
            attribute(ScmCommonLogAttributes.ERROR_TYPE, throwable.getClass().getName());
            attribute(ScmCommonLogAttributes.ERROR_MESSAGE, safeMessage(throwable));
        }
        return this;
    }

    public LogObservationBuilder change() {
        this.recordKind = ObservationRecordKind.CHANGE;
        this.category = "change";
        return this;
    }

    public LogObservationBuilder level(String level) {
        this.level = normalizeLevel(level);
        return this;
    }

    public LogObservationBuilder loggerName(String loggerName) {
        this.loggerName = loggerName;
        return this;
    }

    public LogObservationBuilder loggerName(Class<?> loggerClass) {
        this.loggerName = loggerClass == null ? null : loggerClass.getName();
        return this;
    }

    public LogObservationBuilder threadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public LogObservationBuilder message(String message) {
        this.message = message;
        return this;
    }

    public LogObservationBuilder category(String category) {
        this.category = category;
        return this;
    }

    public LogObservationBuilder correlationType(String correlationType) {
        this.correlationType = correlationType;
        return this;
    }

    public LogObservationBuilder trace(String message) {
        return log("TRACE", message);
    }

    public LogObservationBuilder debug(String message) {
        return log("DEBUG", message);
    }

    public LogObservationBuilder info(String message) {
        return log("INFO", message);
    }

    public LogObservationBuilder warn(String message) {
        return log("WARN", message);
    }

    public LogObservationBuilder error(String message) {
        return log("ERROR", message);
    }

    public LogObservationBuilder warn(String message, Throwable throwable) {
        return log("WARN", message).exception(throwable);
    }

    public LogObservationBuilder error(String message, Throwable throwable) {
        return log("ERROR", message).exception(throwable);
    }

    private LogObservationBuilder log(String level, String message) {
        this.level = normalizeLevel(level);
        this.message = message;
        return this;
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return null;
        }
        String message = SENSITIVE_ASSIGNMENT.matcher(throwable.getMessage()
                        .replace('\r', ' ')
                        .replace('\n', ' '))
                .replaceAll("$1=***")
                .trim();
        return message.length() > MAX_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH)
                : message;
    }

    private String normalizeLevel(String level) {
        return level == null || level.isBlank()
                ? "INFO"
                : level.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public void write() {
        Instant timestamp = observation.now();
        ObservationDocumentBuilder builder = observation.documentFactory().log(
                recordKind,
                throwable != null,
                timestamp,
                level,
                loggerName,
                threadName,
                message,
                correlationId,
                correlationType
        );
        if (recordKind == ObservationRecordKind.CONTEXT) {
            observation.documentFactory().putRuntimeContext(builder);
        }
        putKindFields(builder);
        builder.putAll(attributes);
        LinkedHashMap<String, Object> document = builder.build();
        writeSlf4j(document);
    }

    @Override
    protected LogObservationBuilder self() {
        return this;
    }

    private void putKindFields(ObservationDocumentBuilder builder) {
        if (recordKind == ObservationRecordKind.EVENT
                || recordKind == ObservationRecordKind.CONTEXT
                || recordKind == ObservationRecordKind.CHANGE) {
            builder.put(ScmCommonLogAttributes.EVENT_CATEGORY, textOrDefault(category, "application"));
            builder.put(ScmCommonLogAttributes.EVENT_ACTION, textOrDefault(action, "log.event"));
            builder.put(ScmCommonLogAttributes.EVENT_OUTCOME, textOrDefault(outcome, "unknown"));
        }
    }

    private void writeSlf4j(Map<String, Object> document) {
        Logger logger = LoggerFactory.getLogger(textOrDefault(loggerName, "application"));
        Marker marker = ScmLogMarkers.recordKindMarker(recordKind);
        Object[] arguments = structuredArguments(document);
        if (throwable != null) {
            arguments = appendThrowable(arguments, throwable);
        }
        switch (level) {
            case "TRACE" -> {
                if (marker == null) {
                    logger.trace(message, arguments);
                } else {
                    logger.trace(marker, message, arguments);
                }
            }
            case "DEBUG" -> {
                if (marker == null) {
                    logger.debug(message, arguments);
                } else {
                    logger.debug(marker, message, arguments);
                }
            }
            case "WARN" -> {
                if (marker == null) {
                    logger.warn(message, arguments);
                } else {
                    logger.warn(marker, message, arguments);
                }
            }
            case "ERROR" -> {
                if (marker == null) {
                    logger.error(message, arguments);
                } else {
                    logger.error(marker, message, arguments);
                }
            }
            default -> {
                if (marker == null) {
                    logger.info(message, arguments);
                } else {
                    logger.info(marker, message, arguments);
                }
            }
        }
    }

    private Object[] structuredArguments(Map<String, Object> document) {
        List<Object> arguments = new ArrayList<>();
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (!isEventOwnedField(entry.getKey())) {
                arguments.add(StructuredArguments.kv(entry.getKey(), entry.getValue()));
            }
        }
        return arguments.toArray();
    }

    private boolean isEventOwnedField(String fieldName) {
        return ScmCommonLogAttributes.TIMESTAMP.name().equals(fieldName)
                || ScmCommonLogAttributes.LOG_LEVEL.name().equals(fieldName)
                || ScmCommonLogAttributes.LOG_LOGGER.name().equals(fieldName)
                || ScmCommonLogAttributes.PROCESS_THREAD_NAME.name().equals(fieldName)
                || ScmCommonLogAttributes.MESSAGE.name().equals(fieldName);
    }

    private Object[] appendThrowable(Object[] arguments, Throwable throwable) {
        Object[] result = new Object[arguments.length + 1];
        System.arraycopy(arguments, 0, result, 0, arguments.length);
        result[arguments.length] = throwable;
        return result;
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
