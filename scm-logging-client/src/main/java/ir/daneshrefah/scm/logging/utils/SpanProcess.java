package ir.daneshrefah.scm.logging.utils;

import io.opentelemetry.api.trace.Span;

@FunctionalInterface
public interface SpanProcess {
    void process(Span span);
}
