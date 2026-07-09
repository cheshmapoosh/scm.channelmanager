package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SafeFailureMessageFormatter {
    private final ObservationSanitizer sanitizer;

    public String format(Throwable throwable, int maxLength) {
        if (throwable == null || throwable.getMessage() == null) {
            return null;
        }
        String message = throwable.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
        if (message.isBlank()) {
            return null;
        }
        Object sanitized = sanitizer.sanitize("error.message", message);
        message = sanitized == null ? null : String.valueOf(sanitized).trim();
        if (message == null || message.isBlank()) {
            return null;
        }
        return message.length() <= maxLength ? message : message.substring(0, maxLength);
    }
}
