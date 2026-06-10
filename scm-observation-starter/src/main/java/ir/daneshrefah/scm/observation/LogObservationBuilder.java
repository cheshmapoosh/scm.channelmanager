package ir.daneshrefah.scm.observation;

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

    LogObservationBuilder(ScmObservation observation) {
        super(observation);
        this.action = "log.event";
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
        return log("WARN", message).throwable(throwable);
    }

    public LogObservationBuilder error(String message, Throwable throwable) {
        return log("ERROR", message).throwable(throwable);
    }

    private LogObservationBuilder log(String level, String message) {
        this.level = normalizeLevel(level);
        this.message = message;
        return this;
    }

    private LogObservationBuilder throwable(Throwable throwable) {
        if (throwable != null) {
            attribute("error.type", throwable.getClass().getName());
            attribute("error.message", safeMessage(throwable));
        }
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
        // LOG observation is MDC/context enrichment only. Normal logs must use SLF4J directly.
    }

    @Override
    protected LogObservationBuilder self() {
        return this;
    }
}
