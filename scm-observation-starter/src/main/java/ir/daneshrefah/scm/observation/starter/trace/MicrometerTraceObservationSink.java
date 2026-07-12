package ir.daneshrefah.scm.observation.starter.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.TraceContext;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MicrometerTraceObservationSink implements TraceObservationSink {
    private static final String DEFAULT_SPAN_NAME = "trace.span";

    private final Tracer tracer;
    private final ObservationSanitizer sanitizer;
    private final ObservationAttributeRegistry attributeRegistry;
    private final Clock clock;

    /**
     * @deprecated Prefer explicit injection of the application-wide registry.
     */
    @Deprecated(forRemoval = false)
    public MicrometerTraceObservationSink(Tracer tracer, ObservationSanitizer sanitizer) {
        this(tracer, sanitizer, requiredApplicationRegistry());
    }

    public MicrometerTraceObservationSink(
            Tracer tracer,
            ObservationSanitizer sanitizer,
            ObservationAttributeRegistry attributeRegistry
    ) {
        this(tracer, sanitizer, attributeRegistry, Clock.systemUTC());
    }

    public MicrometerTraceObservationSink(
            Tracer tracer,
            ObservationSanitizer sanitizer,
            ObservationAttributeRegistry attributeRegistry,
            Clock clock
    ) {
        this.tracer = tracer;
        this.sanitizer = sanitizer;
        this.attributeRegistry = Objects.requireNonNull(
                attributeRegistry, "The application-wide observation attribute registry is required");
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    private static ObservationAttributeRegistry requiredApplicationRegistry() {
        return ObservationAttributeRegistryHolder.get().orElseThrow(() -> new IllegalStateException(
                "The application-wide observation attribute registry must be initialized before the Micrometer trace sink"));
    }

    @Override
    public TraceObservationHandle start(TraceObservationSpec spec) {
        if (tracer == null || spec == null) {
            return TraceObservationHandle.NOOP;
        }
        try {
            Span.Builder spanBuilder = tracer.spanBuilder().name(textOrDefault(spec.spanName(), DEFAULT_SPAN_NAME));
            configureParent(spanBuilder, spec);
            Span.Kind spanKind = spanKind(spec.spanKind());
            if (spanKind != null) {
                spanBuilder.kind(spanKind);
            }
            tag(spanBuilder, "event.action", textOrDefault(spec.action(), textOrDefault(spec.spanName(), DEFAULT_SPAN_NAME)));
            if (spec.outcome() != null && !spec.outcome().isBlank() && !"unknown".equalsIgnoreCase(spec.outcome().trim())) {
                tag(spanBuilder, "event.outcome", spec.outcome());
            }
            tag(spanBuilder, "correlation.id", spec.correlationId());
            tag(spanBuilder, "correlation.type", spec.correlationType());
            tagAll(spanBuilder, spec.spanName(), spec.attributes());
            Span span = spanBuilder.start();
            return new MicrometerTraceObservationHandle(span, spec, System.nanoTime());
        } catch (RuntimeException ex) {
            return TraceObservationHandle.NOOP;
        }
    }

    private void configureParent(Span.Builder spanBuilder, TraceObservationSpec spec) {
        String traceId = textOrNull(spec.traceId());
        String parentSpanId = textOrNull(spec.parentSpanId());
        if (traceId == null || parentSpanId == null) {
            spanBuilder.setNoParent();
            return;
        }
        io.micrometer.tracing.TraceContext parent = tracer.traceContextBuilder()
                .traceId(traceId)
                .spanId(parentSpanId)
                .sampled(Boolean.TRUE)
                .build();
        spanBuilder.setParent(parent);
    }

    private Map<String, Object> eventAttributes(Map<String, ?> attributes) {
        Map<String, Object> safeAttributes = new LinkedHashMap<>();
        if (attributes == null || attributes.isEmpty()) {
            return safeAttributes;
        }
        attributes.forEach((key, value) -> {
            if (key == null
                    || key.isBlank()
                    || value == null
                    || !TraceAttributeSecurity.isAllowedSpanEventAttribute(key)) {
                return;
            }
            Object sanitized = sanitizer == null ? value : sanitizer.sanitize(key.trim(), value);
            Object prepared = attributeRegistry.prepareValue(ObservationStream.TRACE, key.trim(), sanitized);
            if (prepared != null) {
                safeAttributes.put(key.trim(), prepared);
            }
        });
        return safeAttributes;
    }

    private void tagAll(Span.Builder spanBuilder, String spanName, Map<String, Object> attributes) {
        if (attributes == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            if (isSpanAttributeAllowed(spanName, entry.getKey())) {
                tag(spanBuilder, entry.getKey(), entry.getValue());
            }
        }
    }

    private void tagAll(Span span, String spanName, Map<String, Object> attributes) {
        if (attributes == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            if (isSpanAttributeAllowed(spanName, entry.getKey())) {
                tag(span, entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean isSpanAttributeAllowed(String spanName, String fieldName) {
        return fieldName != null
                && !TraceAttributeSecurity.isReservedTraceField(fieldName)
                && ("gateway.receive".equals(textOrNull(spanName))
                || !TraceAttributeSecurity.isGatewayOnlyJwtContextField(fieldName));
    }

    private void tag(Span.Builder spanBuilder, String name, Object value) {
        if (spanBuilder == null || name == null || name.isBlank() || value == null || !TraceAttributeSecurity.isAllowed(name)) {
            return;
        }
        Object prepared = preparedValue(name, value);
        if (prepared == null) {
            return;
        }
        String key = name.trim();
        if (prepared instanceof Boolean booleanValue) {
            spanBuilder.tag(key, booleanValue);
        } else if (prepared instanceof Byte || prepared instanceof Short || prepared instanceof Integer || prepared instanceof Long) {
            spanBuilder.tag(key, ((Number) prepared).longValue());
        } else if (prepared instanceof Float || prepared instanceof Double) {
            spanBuilder.tag(key, ((Number) prepared).doubleValue());
        } else if (prepared instanceof Iterable<?> iterable) {
            tagIterable(spanBuilder, key, iterable);
        } else {
            spanBuilder.tag(key, String.valueOf(prepared));
        }
    }

    private void tag(Span span, String name, Object value) {
        if (span == null || name == null || name.isBlank() || value == null || !TraceAttributeSecurity.isAllowed(name)) {
            return;
        }
        Object prepared = preparedValue(name, value);
        if (prepared == null) {
            return;
        }
        String key = name.trim();
        if (prepared instanceof Boolean booleanValue) {
            span.tag(key, booleanValue);
        } else if (prepared instanceof Byte || prepared instanceof Short || prepared instanceof Integer || prepared instanceof Long) {
            span.tag(key, ((Number) prepared).longValue());
        } else if (prepared instanceof Float || prepared instanceof Double) {
            span.tag(key, ((Number) prepared).doubleValue());
        } else if (prepared instanceof Iterable<?> iterable) {
            tagIterable(span, key, iterable);
        } else {
            span.tag(key, String.valueOf(prepared));
        }
    }

    private Object preparedValue(String name, Object value) {
        if (name == null || name.isBlank() || value == null || !TraceAttributeSecurity.isAllowed(name)) {
            return null;
        }
        String fieldName = name.trim();
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(fieldName, value);
        return attributeRegistry.prepareValue(ObservationStream.TRACE, fieldName, sanitized);
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

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private final class MicrometerTraceObservationHandle implements TraceObservationHandle {
        private final Span span;
        private final TraceObservationSpec spec;
        private final String spanName;
        private final long startedAtNanos;
        private final Object lifecycleMonitor = new Object();
        private boolean finished;

        private MicrometerTraceObservationHandle(Span span, TraceObservationSpec spec, long startedAtNanos) {
            this.span = span;
            this.spec = spec;
            this.spanName = spec.spanName();
            this.startedAtNanos = startedAtNanos;
        }

        @Override
        public TraceContext traceContext() {
            io.micrometer.tracing.TraceContext actual = span == null ? null : span.context();
            return new TraceContext(
                    firstText(actual == null ? null : actual.traceId(), spec.traceId()),
                    firstText(actual == null ? null : actual.spanId(), spec.spanId()),
                    spec.correlationId(),
                    spec.correlationType()
            );
        }

        @Override
        public void finish(String outcome, Map<String, Object> attributes, Throwable throwable) {
            synchronized (lifecycleMonitor) {
                if (finished) {
                    return;
                }
                finished = true;
            }
            try {
                tag(span, "span.duration_ms", elapsedMillis(startedAtNanos, System.nanoTime()));
                tag(span, "event.outcome", textOrDefault(
                        outcome,
                        textOrDefault(spec.outcome(), throwable == null ? "success" : "failure")
                ));
                tagAll(span, spanName, attributes);
                if (throwable != null) {
                    tag(span, "error.type", throwable.getClass().getName());
                    tag(span, "error.message", safeMessage(throwable));
                }
            } catch (RuntimeException ignored) {
                // Trace failures must not affect business flow.
            } finally {
                endSpan();
            }
        }

        @Override
        public void event(String name, Map<String, ?> attributes) {
            if (span == null || name == null || name.isBlank()) {
                return;
            }
            synchronized (lifecycleMonitor) {
                if (finished) {
                    return;
                }
                try {
                    String safeName = name.replace('\r', ' ').replace('\n', ' ').trim();
                    if (safeName.isEmpty()) {
                        return;
                    }
                    if (safeName.length() > 128) {
                        safeName = safeName.substring(0, 128);
                    }
                    span.event(safeName);
                    if (span.context() != null) {
                        TraceObservationSpanEventRegistry.add(
                                span.context().traceId(),
                                span.context().spanId(),
                                new TraceObservationSpanEvent(safeName, Instant.now(clock), eventAttributes(attributes))
                        );
                    }
                } catch (RuntimeException ignored) {
                    // Trace event failures must not affect business flow.
                }
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

        private long elapsedMillis(long startNanos, long endNanos) {
            return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, endNanos - startNanos));
        }

        private String firstText(String first, String fallback) {
            String value = textOrNull(first);
            return value == null ? fallback : value;
        }

        private String safeMessage(Throwable throwable) {
            if (throwable == null || throwable.getMessage() == null) {
                return null;
            }
            String message = throwable.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
            return message.length() > 300 ? message.substring(0, 300) : message;
        }
    }
}
