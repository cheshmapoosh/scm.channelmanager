# scm-observation-servlet-starter

`scm-observation-servlet-starter` is the opt-in Servlet transport adapter for SCM observation. It depends on the transport-neutral `scm-observation-starter` and owns:

- Servlet request correlation MDC;
- optional HTTP server spans;
- HTTP header, method, path, route, status, and client-address extraction;
- Servlet filter registration and ordering.

The core starter does not depend on this adapter and contains no Servlet or HTTP configuration. SOAP, Camel, TCP, MQ, WebSocket, gRPC, and other transports use separate adapters and the same core observation API.

## Dependency

Only a Servlet-facing host that needs request MDC or HTTP server observation adds:

```gradle
implementation project(':scm-observation-servlet-starter')
```

Adding the adapter does not enable any observation signal. Services that expose only Actuator management HTTP, including `scm-cache`, do not depend on it.

Current opt-in hosts are `scm-web`, `scm-uaa`, `scm-config`, and `scm-cm-connector`. `scm-cache` and the non-HTTP `scm-logging` service do not depend on this adapter.

## Properties

The adapter exclusively owns this external property contract:

| Spring property | Environment variable | Default |
| --- | --- | --- |
| `scm.observation.http.server.enabled` | `SCM_OBS_HTTP_SERVER_ENABLED` | `false` |
| `scm.observation.http.server.mode` | `SCM_OBS_HTTP_SERVER_MODE` | `channel-only` |
| `scm.observation.http.server.span-name` | `SCM_OBS_HTTP_SERVER_SPAN_NAME` | `http.server.request` |

Defaults are stored in `META-INF/scm/servlet-observation-defaults.yml`. `ScmServletObservationDefaultsEnvironmentPostProcessor` loads them at `LOWEST_PRECEDENCE` and adds them last, so command-line arguments, system properties, environment variables, Spring Cloud Config, profiles, and host configuration retain precedence. Hosts must not add `spring.config.import` for this resource.

A service-specific span name remains a host policy:

```yaml
scm:
  observation:
    http:
      server:
        span-name: ${SCM_OBS_HTTP_SERVER_SPAN_NAME:uaa.http.request}
```

## Signal Gating

`ObservationMdcFilter` is registered only when both `scm.observation.enabled=true` and `scm.observation.log.enabled=true`. The core `ObservationSignalPolicy` also gates its runtime behavior.

`HttpServerObservationFilter` is configured only when both `scm.observation.enabled=true` and `scm.observation.trace.enabled=true`. Its registration is enabled only when `scm.observation.http.server.enabled=true` and mode is not `channel-only`. The default `channel-only` mode therefore leaves generic HTTP server spans disabled.

Setting `scm.observation.http.server.enabled=true`, changing its mode, or adding this dependency never enables TRACE. Global observation and the core TRACE policy remain authoritative.

## Filter Behavior

`ObservationMdcFilter` runs on `/*` at `Ordered.HIGHEST_PRECEDENCE + 20`.

- It reads a nonblank `X-Correlation-ID`, trims it, and otherwise generates a local correlation id.
- It places `correlationId`, `scmAppName`, and `scmAppProfile` in MDC while the request is processed.
- It restores prior correlation, trace, and span MDC values after the filter chain.

`HttpServerObservationFilter` runs on `/*` at `Ordered.HIGHEST_PRECEDENCE + 30` when its gates allow registration.

- It creates a server-kind span with the configured span name and `correlation.type=request`.
- Correlation resolution is `X-Correlation-ID`, then current MDC, then a generated local id.
- It records HTTP method, request URI with servlet-path fallback, Spring's best-matching route, response status, and client IP.
- Client IP resolution is the first `X-Forwarded-For` value, then `X-Real-IP`, then the Servlet remote address.
- A thrown or explicitly recorded exception marks the span as failed; an HTTP status of 400 or greater also produces failure outcome.
- Async and error dispatches are not observed again, and a request attribute prevents duplicate filter observation.

Controllers or exception handlers may call `HttpServerObservationFilter.recordException(request, throwable)` so an exception translated into an HTTP response remains attached to the server span. The adapter passes all generated observation data through the core builders, validation, sanitization, and output contract.
