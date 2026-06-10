# scm-observation-starter

`scm-observation-starter` provides shared SCM observation APIs and Spring Boot auto-configuration.

Adding the dependency alone enables nothing. The starter is fully opt-in and fail-closed.

## Signals

The only supported SCM observation namespaces are:

```text
scm.observation.enabled
scm.observation.log.*
scm.observation.trace.*
scm.observation.audit.*
scm.observation.metric.*
```

The master switch and the per-signal switch must both be enabled:

```yaml
scm:
  observation:
    enabled: false
    log:
      enabled: false
    trace:
      enabled: false
    audit:
      enabled: false
    metric:
      enabled: false
```

Effective state:

```text
signal enabled = scm.observation.enabled AND scm.observation.<signal>.enabled
```

`scm.observation.enabled=true` by itself still leaves every signal disabled.

The starter does not support the old signal tree, shared observation file tree, shared observation output tree, separate SCM log tree, or deployment observation metadata.

## Architecture

```text
Normal LOG -> Lombok @Slf4j -> SLF4J -> Logback -> host logback-spring.xml
TRACE      -> starter event -> direct Logback event -> TRACE marker -> host JSONL appender
AUDIT      -> starter event -> direct Logback event -> AUDIT marker -> host JSONL appender
METRIC     -> starter metric API -> Micrometer -> Actuator -> Prometheus
```

Metrics are never written to JSONL files by the starter.

## Normal Logging

Application code must use normal Lombok/SLF4J logging:

```java
@Slf4j
@Component
public class HazelcastBootstrap {
    public void start() {
        log.info("Hazelcast initialization started");
    }
}
```

`LOG` does not replace SLF4J and does not change the Java logging API. Logger names remain the source class names.

`scm.observation.log.*` configures LOG observation behavior and host Logback output settings. Logger levels are still enforced by Logback.

## TRACE And AUDIT Routing

TRACE and AUDIT are structured JSON events. They are routed through Logback markers, not logger names and not SLF4J `info`, `warn`, or `error` calls.

Required marker names:

```text
SCM_OBSERVATION_TRACE
SCM_OBSERVATION_AUDIT
```

Host `logback-spring.xml` must:

- reject these markers from the normal application appender and console appender,
- accept only `SCM_OBSERVATION_TRACE` in the trace JSONL appender,
- accept only `SCM_OBSERVATION_AUDIT` in the audit JSONL appender,
- use `<pattern>%msg%n</pattern>` for trace and audit files.

The starter provides `ir.daneshrefah.scm.observation.logback.ObservationMarkerFilter` for XML routing.

## Configuration Contract

Each signal owns its output settings:

```yaml
scm:
  observation:
    enabled: true

    log:
      enabled: true
      console:
        enabled: false
      file:
        enabled: true
        directory: /var/log/app
        file-name: scm-cache.log
        archive-directory: /var/log/app/archive
      rolling:
        max-file-size: 100MB
        max-history: 30
        total-size-cap: 10GB
        clean-history-on-start: false
      level:
        root: INFO
        application: INFO
        spring: INFO
        hibernate: WARN
        hazelcast: INFO

    trace:
      enabled: true
      file:
        enabled: true
        directory: /var/log/observation
        file-name: scm-cache-trace.jsonl
        archive-directory: /var/log/observation/archive
      rolling:
        max-file-size: 100MB
        max-history: 30
        total-size-cap: 10GB
        clean-history-on-start: false
      async:
        enabled: true
        queue-size: 8192
        discarding-threshold: 0
        never-block: false

    audit:
      enabled: true
      file:
        enabled: true
        directory: /var/log/observation
        file-name: scm-cache-audit.jsonl
        archive-directory: /var/log/observation/archive
      rolling:
        max-file-size: 100MB
        max-history: 90
        total-size-cap: 20GB
        clean-history-on-start: false
      async:
        enabled: false
        queue-size: 8192
        discarding-threshold: 0
        never-block: false

    metric:
      enabled: false
```

Hosts should place development values in `application-default.yml` and non-development values in `scm-config`. The host `logback-spring.xml` reads the effective Spring Environment with `<springProperty>`.

## Deployment Identity

The starter has no deployment metadata feature. It does not read deployment identity variables, infer the operating-system user, look up the hostname, generate runtime identity, or add Kubernetes Pod or Node fields.

Audit actor identity must come from the authenticated application security principal. If no authenticated actor is available, omit the actor attribute.

## Safety Rules

- Do not put request bodies, cache values, tokens, credentials, kubeconfig, or secrets in logs, traces, audits, or metric tags.
- Use predefined typed attributes such as `ScmCacheAttributes.CLUSTER_NAME`.
- Avoid high-cardinality metric tags such as cache key, user ID, request ID, correlation ID, and exception message.
- Prefer synchronous AUDIT output first. If AUDIT is async, configure no-discard behavior and understand backpressure.
- Treat SLF4J TRACE level as a development logging level. It is separate from the structured TRACE observation signal.

## Auto-Configuration

All Spring wiring is provided by the starter. Host modules should not create observation configuration classes, custom sinks, custom signal policies, custom marker filters, Micrometer wiring, or OpenTelemetry wiring.

Disabled observation APIs are safe no-ops: they create no events, serialize nothing, publish nothing, and do not warn per call.
