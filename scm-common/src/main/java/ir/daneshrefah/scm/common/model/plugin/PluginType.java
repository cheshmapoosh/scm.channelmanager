package ir.daneshrefah.scm.common.model.plugin;

public enum PluginType {
    FILTER, // Reject or modify requests before execution. e.g. rate-limit, ip-whitelist
    VALIDATOR, // Validate input, tokens, API keys, etc. e.g. auth-key, jwt-validator
    TRANSFORMER, // Mutate headers, body, or route URI. e.g. add-header, rewrite-uri
    LOGGER, // Log requests or responses. e.g. log-request, audit-log
    METRICS, // Emit Prometheus-compatible metrics. prometheus-exporter, influx-metric
    TRACER, // Trace and correlate requests through the system, opentelemetry-tracer, zipkin-span
}
