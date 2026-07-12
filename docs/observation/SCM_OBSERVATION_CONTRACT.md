# SCM Observation Contract

SCM observability writes LOG, TRACE, and AUDIT as JSONL files by default. JSONL is the structured Filebeat ingestion contract; optional simple `.log` output is human-readable and is not ingested by the current JSONL pipeline. Metrics stay on the Actuator and Micrometer path.

## External Contract

| Variable | Spring property |
| --- | --- |
| `SCM_APP` | `spring.application.name` |
| `SCM_ENV` | `spring.profiles.active` |
| `SCM_LABEL` | `spring.cloud.config.label` for Config Clients |
| `SCM_LABEL` | `spring.cloud.config.server.git.default-label` for `scm-config` |
| `SCM_METADATA_NAMESPACE` | `scm.metadata.namespace` |
| `SCM_METADATA_INSTANCE_ID` | `scm.metadata.instance-id` |
| `SCM_METADATA_TIME_ZONE` | `scm.metadata.time-zone` |

`SCM_ENV` must be exactly one of `dev`, `test`, `pilot`, or `prod`. It is lowercase and case-sensitive. Comma-separated profiles are not part of the SCM contract.

`SCM_LABEL` is the Spring Cloud Config label and may be a branch, tag, or commit. It must be nonblank and must contain exactly one label. The default is `master`.

## Configuration Ownership

`scm-observation-starter` owns transport-neutral technical defaults in `META-INF/scm/observation-defaults.yml`. It loads that resource as a lowest-precedence property source; hosts must not import it with `spring.config.import`. Transport adapters own and load their own defaults by the same low-precedence mechanism.

Effective precedence, highest first:

1. command-line argument
2. system property
3. OS environment variable
4. Spring Cloud Config
5. external or profile-specific application configuration
6. host application configuration
7. starter observation defaults
8. hard fail-safe Java and Logback defaults

Host applications retain only service signal policy, profile behavior, adapter-specific service policy, actual exceptional overrides, and unrelated application configuration. Supported `SCM_OBS_*` environment variables continue to override the relevant starter values.

The global switch is `scm.observation.enabled`. When it is `false`, the starter creates no LOG, TRACE, AUDIT, or METRIC signal, and no structured SCM observation console or file appender writes output. Destination enablement never enables its parent signal.

Ordinary diagnostic logging remains available through the independent `SCM_FALLBACK_CONSOLE` when either `scm.observation.enabled=false` or `scm.observation.log.enabled=false`. The fallback is human-readable, writes no observation file, and excludes SCM TRACE and AUDIT markers. It is inactive when both switches are true, even if structured LOG console and file destinations are explicitly disabled, so it neither duplicates structured LOG output nor overrides explicit destination policy. The Logback hard defaults activate this fallback if starter defaults cannot be loaded.

Default signal policy:

| Signal | Enabled |
| --- | --- |
| LOG | `true` |
| TRACE | `true` |
| AUDIT | `false` |
| METRIC | `false` |

Default destinations:

| Stream | Console enabled | Console format | File enabled | File format |
| --- | --- | --- | --- | --- |
| LOG | `false` | `simple` | `true` | `jsonl` |
| TRACE | `false` | `simple` | `true` | `jsonl` |
| AUDIT | `false` | `simple` | `true` | `jsonl` |

All six `scm.observation.{log,trace,audit}.{console,file}.format` properties accept exactly lowercase, case-sensitive `simple` or `jsonl`. Blank, uppercase, comma-separated, and other values fail startup even if the destination is disabled. If the starter defaults resource is unavailable, hard Java and structured Logback enablement fallbacks are `false`; the ordinary diagnostic fallback console consequently remains available.

The starter README contains the canonical compact host-policy and `dev` profile examples. The `dev` profile enables all three console destinations without enabling the AUDIT signal.

## Module Boundary

| Module | Responsibility |
| --- | --- |
| `scm-observation-starter` | Transport-neutral signal policy, context and scope APIs, LOG/TRACE/AUDIT documents, sanitization, validation, metrics, and Logback integration |
| `scm-observation-servlet-starter` | Servlet request correlation MDC and optional HTTP server observation |

The core starter does not inspect HTTP, Servlet, SOAP, Camel, TCP, MQ, WebSocket, or gRPC traffic. A transport adapter extracts its protocol metadata, supplies protocol and span-kind values, and builds safe attributes through the core observation API. HTTP headers, method, path, route, status, client address, and Servlet filter registration therefore belong exclusively to the Servlet adapter. Other transports can add independent adapters without changing the core.

## Runtime Metadata

Deployable services define:

```yaml
scm:
  metadata:
    namespace: ${SCM_METADATA_NAMESPACE:}
    instance-id: ${SCM_METADATA_INSTANCE_ID:}
    time-zone: ${SCM_METADATA_TIME_ZONE:}
```

Development profiles provide local defaults:

```yaml
scm:
  metadata:
    namespace: ${SCM_METADATA_NAMESPACE:local}
    instance-id: ${SCM_METADATA_INSTANCE_ID:local-scm-web}
```

For `test`, `pilot`, and `prod`, namespace and instance id must be supplied by the runtime. Kubernetes deployments use the Downward API:

```yaml
env:
  - name: SCM_METADATA_NAMESPACE
    valueFrom:
      fieldRef:
        fieldPath: metadata.namespace
  - name: SCM_METADATA_INSTANCE_ID
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
```

`scm.metadata.time-zone` is optional. When it is blank or absent, the JVM system timezone is used for physical file naming metadata. Invalid timezone values fail startup.

## Standard Fields

Every LOG, TRACE, and AUDIT document includes:

```text
event.stream
service.name
deployment.service.name
deployment.service.version
deployment.environment
scm.runtime
scm.metadata.namespace
scm.metadata.instance_id
scm.metadata.time_zone
scm.config.label
scm.observation.target.index
```

`@timestamp` remains UTC. The metadata timezone is not used for JSON event timestamps and is not used for the Elasticsearch index hour.

## Elasticsearch Indexes

Index naming is starter-owned and not configurable by host applications.

With a real business channel:

```text
{stream}-scm-{namespace}-{env}-{channelCode}-{yyyy.MM.dd.HH}
```

Without a real business channel:

```text
{stream}-scm-{namespace}-{env}-{yyyy.MM.dd.HH}
```

The literal `scm` is fixed. Index names are normalized to lowercase and invalid characters are replaced. Missing channel values include null, blank, `unknown`, `default`, `none`, `n/a`, and `n-a`.

Examples:

```text
log-scm-payment-prod-mb-2026.07.11.06
trace-scm-payment-prod-mb-2026.07.11.06
audit-scm-payment-prod-2026.07.11.06
```

The index hour is always UTC.

## File Names

The starter owns the default root directory, base identity pattern, and stream-directory derivation:

```text
root directory:    ${SCM_OBS_ROOT_DIR:${user.home}/scm/obs}
base-name pattern: scm-${spring.application.name}-${spring.profiles.active}-${scm.metadata.namespace}-${scm.metadata.instance-id}
```

Hosts override these only for an actual deployment requirement.

`scm.observation.file.root-directory` controls the shared physical root directory. The stream directory properties `scm.observation.log.file.directory`, `scm.observation.trace.file.directory`, and `scm.observation.audit.file.directory` are optional per-stream overrides. Directory resolution is:

```text
stream-specific directory override -> shared observation root directory -> ${user.home}/scm/obs
```

`scm.observation.file.base-name-pattern` controls only the stable identity part of the filename. The starter owns stream prefix, hour token, roll index, and format-specific extension:

```text
simple: {stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}-{rollIndex}.log
jsonl:  {stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}-{rollIndex}.jsonl
```

Directory layout:

```text
{root}/{appName}/{env}/{namespace}/{stream}/
```

Examples:

```text
log-scm-scm-web-prod-payment-scm-web-7d98c9-20260711-10-0.jsonl
trace-scm-scm-web-prod-payment-scm-web-7d98c9-20260711-10-0.jsonl
audit-scm-scm-web-prod-payment-scm-web-7d98c9-20260711-10-0.jsonl
log-scm-scm-web-dev-local-local-scm-web-20260711-10-0.log
```

File names never include channel code or Config label. Observation files are not gzipped and do not use a separate archive directory. Both formats retain `%d` and `%i` through the hour and roll-index tokens. Current and rolled JSONL files use final names while Filebeat reads them.

## Structured LOG Event Identity

One `ILoggingEvent` maps to one immutable structured LOG document. Resolution uses the event object's identity, not its message, logger, or timestamp, and keeps only weak event references. Concurrent destinations share the same resolution and `ScmLogDocumentFactory` is invoked at most once for that event. Consequently simple and JSONL destinations observe identical `@timestamp`, correlation and trace identifiers, event fields, runtime metadata, sanitized attributes, and exception fields. Two distinct events with identical visible values remain separate documents.

## Simple Output

Every simple line is UTF-8, exactly one physical line, and contains exactly one canonical stream identity: `stream=log`, `stream=trace`, or `stream=audit`. Field order is deterministic, null values are omitted, numeric and boolean values remain unquoted, and textual values are safely quoted and escaped. Simple TRACE and AUDIT are real `key=value` output, not JSON prefixed with a stream name.

If structured simple LOG rendering fails, the encoder emits a sanitized, primitive-only single line containing `stream=log` and `encoding.error=true` instead of empty output. That failure path does not reprocess structured arguments or emit a stack trace.

JSONL files remain the default machine-ingestion format. Simple `.log` files are optional debugging output and must not be added to the current Filebeat JSON parser inputs.

## Legacy Projection

Legacy database projection remains separate from the runtime metadata and file/index naming contract. When `scm.observation.legacy.enabled=true`, both of these fields must be present:

```text
scm.observation.legacy.service.code
scm.observation.legacy.operation.code
```

Host modules decide where legacy projection is appropriate. The starter does not infer legacy service or operation codes from span names.
