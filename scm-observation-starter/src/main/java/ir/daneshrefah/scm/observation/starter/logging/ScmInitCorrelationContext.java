package ir.daneshrefah.scm.observation.starter.logging;

import ir.daneshrefah.scm.observation.starter.CorrelationType;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class ScmInitCorrelationContext {
    private static final AtomicReference<String> CURRENT = new AtomicReference<>();

    private ScmInitCorrelationContext() {
    }

    public static String ensure() {
        String mdcValue = textOrNull(MDC.get(ScmMdcKeys.CORRELATION_ID));
        if (mdcValue != null) {
            CURRENT.set(mdcValue);
            ensureLifecycleType();
            return mdcValue;
        }
        String value = CURRENT.updateAndGet(existing -> existing == null ? UUID.randomUUID().toString() : existing);
        MDC.put(ScmMdcKeys.CORRELATION_ID, value);
        ensureLifecycleType();
        return value;
    }

    public static String current() {
        return CURRENT.get();
    }

    public static void set(String value) {
        String normalized = textOrNull(value);
        if (normalized == null) {
            return;
        }
        CURRENT.set(normalized);
        MDC.put(ScmMdcKeys.CORRELATION_ID, normalized);
        ensureLifecycleType();
    }

    private static void ensureLifecycleType() {
        if (textOrNull(MDC.get(ScmMdcKeys.CORRELATION_TYPE)) == null
                && textOrNull(MDC.get(ScmLogFields.CORRELATION_TYPE)) == null) {
            MDC.put(ScmMdcKeys.CORRELATION_TYPE, CorrelationType.LIFECYCLE.value());
        }
    }

    private static String textOrNull(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
