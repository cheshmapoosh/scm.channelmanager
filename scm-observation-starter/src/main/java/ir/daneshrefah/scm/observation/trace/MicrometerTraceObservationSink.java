package ir.daneshrefah.scm.observation.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import ir.daneshrefah.scm.observation.ObservationSanitizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MicrometerTraceObservationSink implements TraceObservationSink {
    private static final String DEFAULT_SPAN_NAME = "trace.span";

    private final Tracer tracer;
    private final ObservationSanitizer sanitizer;

    public MicrometerTraceObservationSink(Tracer tracer, ObservationSanitizer sanitizer) {
        this.tracer = tracer;
        this.sanitizer = sanitizer;
    }

    @Override
    public TraceObservationHandle start(TraceObservationSpec spec) {
        if (tracer == null || spec == null) {
            return TraceObservationHandle.NOOP;
        }
        try {
            Span.Builder spanBuilder = tracer.spanBuilder().name(textOrDefault(spec.spanName(), DEFAULT_SPAN_NAME));
            Span.Kind spanKind = spanKind(spec.spanKind());
            if (spanKind != null) {
                spanBuilder.kind(spanKind);
            }
            tag(spanBuilder, "event.action", textOrDefault(spec.action(), textOrDefault(spec.spanName(), DEFAULT_SPAN_NAME)));
            if (spec.outcome() != null && !spec.outcome().isBlank() && !"unknown".equalsIgnoreCase(spec.outcome().trim())) {
                tag(spanBuilder, "event.outcome", spec.outcome());
            }
            tag(spanBuilder, "correlation.id", spec.correlationId());
            tagAll(spanBuilder, spec.attributes());
            Span span = spanBuilder.start();
            Tracer.SpanInScope spanInScope = tracer.withSpan(span);
            return new MicrometerTraceObservationHandle(span, spanInScope);
        } catch (RuntimeException ex) {
            return TraceObservationHandle.NOOP;
        }
    }

    private void tagAll(Span.Builder spanBuilder, Map<String, Object> attributes) {
        if (attributes == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            tag(spanBuilder, entry.getKey(), entry.getValue());
        }
    }

    private void tag(Span.Builder spanBuilder, String name, Object value) {
        if (spanBuilder == null || name == null || name.isBlank() || value == null || !TraceAttributeSecurity.isAllowed(name)) {
            return;
        }
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(name.trim(), value);
        if (sanitized == null) {
            return;
        }
        String key = name.trim();
        if (sanitized instanceof Boolean booleanValue) {
            spanBuilder.tag(key, booleanValue);
        } else if (sanitized instanceof Byte || sanitized instanceof Short || sanitized instanceof Integer || sanitized instanceof Long) {
            spanBuilder.tag(key, ((Number) sanitized).longValue());
        } else if (sanitized instanceof Float || sanitized instanceof Double) {
            spanBuilder.tag(key, ((Number) sanitized).doubleValue());
        } else if (sanitized instanceof Iterable<?> iterable) {
            tagIterable(spanBuilder, key, iterable);
        } else {
            spanBuilder.tag(key, String.valueOf(sanitized));
        }
    }

    private void tag(Span span, String name, Object value) {
        if (span == null || name == null || name.isBlank() || value == null || !TraceAttributeSecurity.isAllowed(name)) {
            return;
        }
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(name.trim(), value);
        if (sanitized == null) {
            return;
        }
        String key = name.trim();
        if (sanitized instanceof Boolean booleanValue) {
            span.tag(key, booleanValue);
        } else if (sanitized instanceof Byte || sanitized instanceof Short || sanitized instanceof Integer || sanitized instanceof Long) {
            span.tag(key, ((Number) sanitized).longValue());
        } else if (sanitized instanceof Float || sanitized instanceof Double) {
            span.tag(key, ((Number) sanitized).doubleValue());
        } else if (sanitized instanceof Iterable<?> iterable) {
            tagIterable(span, key, iterable);
        } else {
            span.tag(key, String.valueOf(sanitized));
        }
    }

    private void tagIterable(Span.Builder spanBuilder, String key, Iterable<?> iterable) {
        List<Object> values = iterableValues(iterable);
        if (values.isEmpty()) {
            return;
        }
        Object first = values.get(0);
        if (first instanceof Boolean) {
            spanBuilder.tagOfBooleans(key, values.stream().filter(Boolean.class::isInstance).map(Boolean.class::cast).toList());
        } else if (first instanceof Byte || first instanceof Short || first instanceof Integer || first instanceof Long) {
            spanBuilder.tagOfLongs(key, values.stream().filter(Number.class::isInstance).map(Number.class::cast).map(Number::longValue).toList());
        } else if (first instanceof Float || first instanceof Double) {
            spanBuilder.tagOfDoubles(key, values.stream().filter(Number.class::isInstance).map(Number.class::cast).map(Number::doubleValue).toList());
        } else {
            spanBuilder.tagOfStrings(key, values.stream().map(String::valueOf).toList());
        }
    }

    private void tagIterable(Span span, String key, Iterable<?> iterable) {
        List<Object> values = iterableValues(iterable);
        if (values.isEmpty()) {
            return;
        }
        Object first = values.get(0);
        if (first instanceof Boolean) {
            span.tagOfBooleans(key, values.stream().filter(Boolean.class::isInstance).map(Boolean.class::cast).toList());
        } else if (first instanceof Byte || first instanceof Short || first instanceof Integer || first instanceof Long) {
            span.tagOfLongs(key, values.stream().filter(Number.class::isInstance).map(Number.class::cast).map(Number::longValue).toList());
        } else if (first instanceof Float || first instanceof Double) {
            span.tagOfDoubles(key, values.stream().filter(Number.class::isInstance).map(Number.class::cast).map(Number::doubleValue).toList());
        } else {
            span.tagOfStrings(key, values.stream().map(String::valueOf).toList());
        }
    }

    private List<Object> iterableValues(Iterable<?> iterable) {
        List<Object> values = new ArrayList<>();
        for (Object value : iterable) {
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private Span.Kind spanKind(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "server" -> Span.Kind.SERVER;
            case "client" -> Span.Kind.CLIENT;
            case "producer" -> Span.Kind.PRODUCER;
            case "consumer" -> Span.Kind.CONSUMER;
            default -> null;
        };
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private final class MicrometerTraceObservationHandle implements TraceObservationHandle {
        private final Span span;
        private final Tracer.SpanInScope spanInScope;
        private boolean finished;

        private MicrometerTraceObservationHandle(Span span, Tracer.SpanInScope spanInScope) {
            this.span = span;
            this.spanInScope = spanInScope;
        }

        @Override
        public void finish(String outcome, Map<String, Object> attributes, Throwable throwable) {
            if (finished) {
                return;
            }
            finished = true;
            try {
                if (outcome != null && !outcome.isBlank()) {
                    tag(span, "event.outcome", outcome);
                }
                if (attributes != null) {
                    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                        tag(span, entry.getKey(), entry.getValue());
                    }
                }
                if (throwable != null) {
                    span.error(throwable);
                }
            } catch (RuntimeException ignored) {
                // Trace failures must not affect business flow.
            } finally {
                closeScope();
                endSpan();
            }
        }

        private void closeScope() {
            try {
                if (spanInScope != null) {
                    spanInScope.close();
                }
            } catch (RuntimeException ignored) {
                // Trace failures must not affect business flow.
            }
        }

        private void endSpan() {
            try {
                if (span != null) {
                    span.end();
                }
            } catch (RuntimeException ignored) {
                // Trace failures must not affect business flow.
            }
        }
    }
}
