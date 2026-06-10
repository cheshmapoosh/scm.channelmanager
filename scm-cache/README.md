# scm-cache

`scm-cache` is the SCM Hazelcast member process. It is infrastructure only.

It does not expose REST APIs, OpenAPI, Spring Security filters, user-cache integration, session-cache integration, or a Hazelcast client. It starts as a non-web Spring Boot application and exposes only the Hazelcast member port.

## Configuration Source

Local development uses:

```text
scm-cache/src/main/resources/application-default.yml
```

Non-development environments must use Spring Cloud Config through `scm-config`. Do not add local production profile files for observation settings.

## Enabled Signals

SCM Cache uses:

```text
LOG    = enabled
TRACE  = enabled
AUDIT  = disabled
METRIC = enabled
```

Both the master switch and the signal switch are required:

```yaml
scm:
  observation:
    enabled: true
    log:
      enabled: true
    trace:
      enabled: true
    audit:
      enabled: false
    metric:
      enabled: true
```

There is no `signals.*`, `files.*`, `output.*`, `scm.log.*`, or deployment observation hierarchy.

## Normal Logs

Normal application logs use Lombok `@Slf4j` and SLF4J. They go through Logback and keep the source class logger name.

INFO is used for production milestones:

- Hazelcast initialization started,
- cache definitions loaded as a count,
- Hazelcast configuration elements registered as a count and summary,
- Hazelcast member started,
- distributed objects materialized as a count,
- initialization completed with duration and counts,
- initialization failed with the throwable preserved,
- application ready,
- shutdown started.

Do not emit one INFO log per cache object. Use DEBUG for safe development details and temporary diagnostics. SLF4J TRACE is separate from the structured TRACE observation signal.

Never log cache keys, cache values, request bodies, tokens, OTPs, passwords, kubeconfig, full network configuration, or sensitive command-line arguments.

## TRACE

TRACE records cache-server lifecycle and Hazelcast initialization events such as:

```text
scm.cache.server.started
scm.cache.hazelcast.member.start
scm.cache.hazelcast.member.shutdown
scm.cache.map.config.load
scm.cache.distributed.object.create
```

Trace output is JSONL through the `SCM_OBSERVATION_TRACE` Logback marker when trace file output is enabled.

## AUDIT

SCM Cache has no business or administrative REST API in this design, so AUDIT is disabled by default. Audit belongs in services that accept authenticated administrative or business requests.

## METRIC

SCM Cache records low-cardinality Micrometer metrics for startup, lifecycle, map configuration loading, distributed object creation, and errors.

Metric tags must stay low-cardinality. Do not use cache key, user ID, request ID, correlation ID, exception message, token, OTP, nickname, terminal code, or cache values as tags.

## Logback

`scm-cache/src/main/resources/logback-spring.xml` is shared by all environments and reads only:

```text
scm.observation.log.*
scm.observation.trace.*
scm.observation.audit.*
```

The application and console appenders reject `SCM_OBSERVATION_TRACE` and `SCM_OBSERVATION_AUDIT`. The trace appender accepts only `SCM_OBSERVATION_TRACE`. The audit appender accepts only `SCM_OBSERVATION_AUDIT`.

Trace and audit JSONL encoders use:

```xml
<pattern>%msg%n</pattern>
```

The SCM Cache package logger reads `scm.observation.log.level.application`. Development can use:

```text
SCM_OBSERVATION_LOG_APPLICATION_LEVEL=DEBUG
```

Temporary targeted troubleshooting may use:

```text
SCM_OBSERVATION_LOG_APPLICATION_LEVEL=TRACE
```

Do not enable DEBUG globally through the root logger.

## Hazelcast Configuration Prefix

Hazelcast member settings are bound from:

```yaml
scm:
  cache:
    hazelcast:
      member:
        instance-name: scm-cache
        cluster-name: scm-cache-cluster
```

Do not use `hazelcast.config` or `scm.cache.hazelcast.config`.
