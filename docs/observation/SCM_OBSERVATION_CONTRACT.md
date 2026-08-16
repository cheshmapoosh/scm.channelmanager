# SCM Observation Contract

SCM observability writes LOG, TRACE, and AUDIT as JSONL files. JSONL is the structured Filebeat ingestion contract. Simple output is console-only. Metrics stay on the Actuator and Micrometer path.

## External Contract

| Variable | Spring property |
| --- | --- |
| `SCM_ENV` | `spring.profiles.active` |
| `SCM_METADATA_NAMESPACE` | `scm.metadata.namespace` |
| `SCM_METADATA_INSTANCE_ID` | `scm.metadata.instance-id` |
| `SCM_METADATA_TIME_ZONE` | `scm.metadata.time-zone` |

`SCM_ENV` must be exactly one of `dev`, `test`, `pilot`, or `prod`. It is lowercase and case-sensitive. Comma-separated profiles are not part of the SCM contract.

## Configuration Ownership

`scm-observation-starter` owns transport-neutral technical mechanics and passive defaults in `META-INF/scm/observation-defaults.yml`. It does not own application identity or Spring Cloud Config labels. Hosts own their baseline signal and sink policy in `application.yml`; deployments supply bootstrap and filesystem inputs.

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
| LOG | `false` |
| TRACE | `false` |
| AUDIT | `false` |
| METRIC | `false` |

Default destinations:

| Stream | Console enabled | Console format | File enabled | File format |
| --- | --- | --- | --- | --- |
| LOG | `false` | `simple` | `false` | fixed `jsonl` |
| TRACE | `false` | `simple` | `false` | fixed `jsonl` |
| AUDIT | `false` | `simple` | `false` | fixed `jsonl` |

Only `scm.observation.{log,trace,audit}.console.format` accepts lowercase, case-sensitive `simple` or `jsonl`. File format is fixed to JSONL. If the starter defaults resource is unavailable, hard Java and structured Logback enablement fallbacks are `false`; the ordinary diagnostic fallback console consequently remains available.

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

TRACE documents use `event.stream=trace` without a redundant `event.category=trace`. `event.action` and `event.outcome` remain required trace fields.

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

The starter owns the deterministic root directory and stream-directory derivation:

```text
root directory: ${SCM_OBS_ROOT_DIR:/var/scm/observation}
archive name:   ${SCM_OBS_ARCHIVE_DIRECTORY_NAME:archive}
```

`scm.observation.file.archive-directory-name` is a directory name only. Blank values, `.`, `..`, absolute paths, and values containing `/` or `\\` are rejected. Signal directories and file names are fixed:

```text
signal directory: {root}/{stream}-{appName}-{env}-{namespace}/
active JSONL:     {stream}-{instanceId}-{env}-{namespace}.jsonl
rolled JSONL:     {signal-directory}/{archive-name}/{stream}-{instanceId}-{env}-{namespace}-{yyyyMMdd-HH}-{rollIndex}.jsonl
```

Examples:

```text
/var/scm/observation/log-scm-web-prod-scm/log-scm-web-7fd86c-x2m4-prod-scm.jsonl
/var/scm/observation/trace-scm-web-prod-scm/archive/trace-scm-web-7fd86c-x2m4-prod-scm-20260815-16-0.jsonl
```

File names never include channel code or Config label. Observation files are not gzipped. The active file contains neither `%d` nor `%i`; only rolled patterns retain those tokens. Active and rolled files use final JSONL names while Filebeat reads them.

## Structured LOG Event Identity

One `ILoggingEvent` maps to one immutable structured LOG document. Resolution uses the event object's identity, not its message, logger, or timestamp, and keeps only weak event references. Concurrent destinations share the same resolution and `ScmLogDocumentFactory` is invoked at most once for that event. Consequently simple and JSONL destinations observe identical `@timestamp`, correlation and trace identifiers, event fields, runtime metadata, sanitized attributes, and exception fields. Two distinct events with identical visible values remain separate documents.

## Simple Output

Every simple line is UTF-8, exactly one physical line, and contains exactly one canonical stream identity: `stream=log`, `stream=trace`, or `stream=audit`. Field order is deterministic, null values are omitted, numeric and boolean values remain unquoted, and textual values are safely quoted and escaped. Simple TRACE and AUDIT are real `key=value` output, not JSON prefixed with a stream name.

If structured simple LOG rendering fails, the encoder emits a sanitized, primitive-only single line containing `stream=log` and `encoding.error=true` instead of empty output. That failure path does not reprocess structured arguments or emit a stack trace.

JSONL files are the machine-ingestion format. Simple console output is optional debugging output and is not part of the Filebeat JSON parser inputs.

## Legacy Projection

Legacy database projection remains separate from the runtime metadata and file/index naming contract. When `scm.observation.legacy.enabled=true`, both of these fields must be present:

```text
scm.observation.legacy.service.code
scm.observation.legacy.operation.code
```

Host modules decide where legacy projection is appropriate. The starter does not infer legacy service or operation codes from span names.
