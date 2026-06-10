# SCM Observation Contract

## Purpose

This contract defines the shared observation output model for SCM and legacy CM. SCM and CM must produce compatible log, trace, and audit events as JSONL files, while metrics remain a separate observability signal exposed through Micrometer/Actuator and scraped by Prometheus.

The JSONL files are written locally and shipped through the final delivery pipeline for analytics/search and, where required, legacy table projection.

This spiral defines documentation and examples only. It does not define Java implementation, dependencies, runtime wiring, Kafka topic design, or an observation starter module.

## Supported Streams

The JSONL observation streams are:

| Stream | Purpose |
| --- | --- |
| `log` | Application log events. |
| `trace` | Distributed trace span events and trace-based service transaction projection events. |
| `audit` | Service/business audit and user/admin/config/security change audit events. |

Metrics are part of SCM observability, but metrics are not JSONL streams.

## Supported Platforms

| Platform | Meaning |
| --- | --- |
| `scm` | New SCM platform. |
| `cm` | Legacy CM platform. |

## Observation Delivery Pipeline

Trace and audit analytics/search path:

```text
JSONL file -> Filebeat -> Kafka -> Logstash -> Elasticsearch -> Kibana
```

Trace and audit legacy compatibility path:

```text
JSONL file -> Filebeat -> Kafka -> scm-logging -> legacy tables
```

Log analytics/search path:

```text
JSONL file -> Filebeat -> Kafka -> Logstash -> Elasticsearch -> Kibana
```

Observability signals:

| Signal | Delivery |
| --- | --- |
| `log` | JSONL -> Filebeat -> Kafka -> Logstash -> Elasticsearch -> Kibana |
| `trace` | JSONL -> Filebeat -> Kafka -> Logstash -> Elasticsearch -> Kibana, and optionally `scm-logging` legacy transaction projection |
| `audit` | JSONL -> Filebeat -> Kafka -> Logstash -> Elasticsearch -> Kibana, and optionally `scm-logging` user action projection |
| `metric` | Micrometer/Actuator -> Prometheus -> Grafana |

Rules:

- `log`, `trace`, and `audit` are the only JSONL observation streams in this contract.
- `metric` is a separate signal and must not create JSONL sample files, JSONL folders, `scm.target.index` values, or metric target indexes.
- Kafka topic requirements are out of scope for this spiral.
- `scm-logging` consumes trace and audit JSONL events from Kafka and projects only explicitly targeted events to legacy tables.
- Legacy transaction tables are populated from trace events, not from audit events.
- Audit events with `scm.audit.type=CHANGE` may be projected to `cm.user_action_log`.

## SCM JSONL Writing Model

SCM Java 21 uses the standard Spring Boot logging stack for observation JSONL file writing:

```text
ScmObservation -> document builder -> compact JSON serializer -> dedicated SLF4J logger -> Logback AsyncAppender -> Logback RollingFileAppender / TimeBasedRollingPolicy -> hourly JSONL file
```

Dedicated SCM observation logger names:

- `ir.daneshrefah.scm.observation.log`
- `ir.daneshrefah.scm.observation.trace`
- `ir.daneshrefah.scm.observation.audit`

Rules:

- Observation loggers must be isolated from normal application logging with `additivity=false`.
- Observation appenders must use a message-only Logback pattern: `%msg%n`.
- Observation file appenders are wrapped with standard Logback `AsyncAppender`.
- Logback must not add timestamp, level, logger name, or JSON wrappers around observation messages.
- The observation code serializes each complete JSON document before it is sent to SLF4J and explicitly disables pretty printing.
- SCM must use Logback standard rolling capabilities for hourly active files; SCM must not implement custom low-level file writing or custom rollover logic.
- Target index hour buckets and observation file hour buckets use `scm.observation.time-zone`; the default is `UTC`.

## Future CM Writing Model

CM Java 7 observation is out of scope for this spiral. A later CM implementation must use this shape:

```text
LegacyObservation -> org.apache.commons.logging.Log -> Log4j 1.2.14 compatible rolling file appender -> hourly JSONL files
```

Rules for the future CM implementation:

- CM business code must use `org.apache.commons.logging.Log`, not direct `org.apache.log4j.Logger`.
- Keep Log4j at `1.2.14` for this compatibility path.
- Do not migrate CM to Logback or Log4j2 as part of the observation rollout.
- Prefer a production-safer compatible Log4j 1.x rolling companion appender over `DailyRollingFileAppender` if a small compatible dependency can be safely added later.
- Do not use `JMSAppender`, `SocketAppender`, `SMTPAppender`, or `JDBCAppender` for observation.
- Observation appenders must be file-only and use message-only layout equivalent to `%m%n`.

## Metrics Contract

Metrics are part of SCM observability but are not written to JSONL files.

Rules:

- SCM Java 21 services expose metrics through Micrometer and Spring Boot Actuator.
- Prometheus scrapes metrics from the Actuator metrics endpoint.
- Grafana is used for metric dashboards and alerting.
- CM Java 7 metrics are out of scope for the initial legacy rollout.
- CM derived metrics may be calculated from trace and audit documents in Elasticsearch.
- A future CM metric adapter may be added later if required.

Recommended metric labels:

| Label | Description |
| --- | --- |
| `app_name` | Application name. |
| `app_profile` | Runtime profile or environment label. |
| `app_label` | Low-cardinality deployment label. |
| `platform` | `scm` or `cm`. |
| `channel_code` | Channel code. |
| `gateway_name` | Gateway name. |
| `service_code` | Service code. |
| `operation_code` | Operation code. |
| `provider_code` | Provider code. |
| `provider_type` | Provider type. |
| `outcome` | Result label such as `success` or `failure`. |
| `error_code` | Safe error/status code. |

Recommended SCM metric names:

- `scm.requests`
- `scm.request.duration`
- `scm.gateway.requests`
- `scm.service.executions`
- `scm.service.duration`
- `scm.operation.calls`
- `scm.operation.duration`
- `scm.provider.calls`
- `scm.provider.duration`
- `scm.plugin.executions`
- `scm.transform.executions`
- `scm.faults`

SCM Java 21 exposes typed Java constants for the shared attribute catalog. Future CM Java 7 can provide compatible constants in its own implementation without changing the field names.

## JSONL Rules

- Files are UTF-8 JSONL.
- Each line is one complete compact JSON object.
- Do not pretty-print JSONL files.
- Each event must include `scm.target.index`.
- Each event must include `scm.target.legacy.enabled`.
- `scm.target.legacy.table` must exist only when `scm.target.legacy.enabled=true`.
- Omit fields that are not applicable instead of writing `null`, unless a downstream consumer explicitly requires the field.
- Field names are case-sensitive.
- Timestamps use ISO-8601 UTC format where possible, for example `2026-06-10T14:15:30.123Z`.
- Numeric durations use milliseconds.

## File Path Contract

Active file:

```text
/var/obs/${APP_NAME}/${APP_PROFILE}/${STREAM}/${STREAM}-scm-${APP_NAME}-${APP_PROFILE}-${APP_LABEL}-${INSTANCE_ID}-${yyyyMMdd-HH}.jsonl
```

Archive file:

```text
/var/obs/${APP_NAME}/${APP_PROFILE}/${STREAM}/archive/${STREAM}-scm-${APP_NAME}-${APP_PROFILE}-${APP_LABEL}-${INSTANCE_ID}-${yyyyMMdd-HH}.jsonl
```

Notes:

- `${STREAM}` is one of `log`, `trace`, or `audit`.
- `${APP_NAME}` is the emitting application name, for example `scm-web` or `cm`.
- `${APP_PROFILE}` is the runtime profile or environment label, for example `prod`.
- `${APP_LABEL}` is a low-cardinality deployment label, commonly the channel label such as `mobile`.
- `${INSTANCE_ID}` identifies the pod, VM, or legacy node, for example `pod01` or `cm01`.
- The `-scm-` segment is the compatibility namespace in the file name and stays literal for both SCM and CM emitters.
- Archive files use the same file name contract under the stream `archive` directory.

Examples:

```text
/var/obs/scm-web/prod/trace/trace-scm-scm-web-prod-mobile-pod01-20260610-14.jsonl
/var/obs/scm-web/prod/log/log-scm-scm-web-prod-mobile-pod01-20260610-14.jsonl
/var/obs/scm-web/prod/audit/audit-scm-scm-web-prod-mobile-pod01-20260610-14.jsonl
/var/obs/cm/prod/trace/trace-scm-cm-prod-mobile-cm01-20260610-14.jsonl
```

## Target Fields

Every JSONL observation event must include:

```text
scm.target.index
scm.target.legacy.enabled
```

`scm.target.index` is mandatory for every JSONL event and is used for Elasticsearch and analytics routing.

Index pattern:

```text
{stream}-{platform}-{channelCode}-{env}-{yyyy.MM.dd.HH}
```

Examples:

```text
trace-scm-mobile-prod-2026.06.10.14
audit-scm-mobile-prod-2026.06.10.14
log-scm-mobile-prod-2026.06.10.14
trace-cm-mobile-prod-2026.06.10.14
```

Index rules:

- `stream` is `log`, `trace`, or `audit`.
- `platform` is `scm` or `cm`.
- `channelCode` is the normalized channel code, for example `mobile`.
- `env` is the normalized deployment environment, for example `prod`.
- The hour bucket is based on the event timestamp.
- The hour bucket uses `scm.observation.time-zone`; the default is `UTC`.
- Application code must treat this as a routing hint, not as an Elastic-specific API contract.

Legacy projection target fields:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `scm.target.legacy.enabled` | boolean | Yes | Whether this event must be projected by `scm-logging` to a legacy table. |
| `scm.target.legacy.table` | keyword | Conditional | Required only when `scm.target.legacy.enabled=true`. Allowed values are `ib.transaction`, `rb.message_log`, `cm.transaction_log`, and `cm.user_action_log`. |

Defaults:

- Log events set `scm.target.legacy.enabled=false`.
- Trace events set `scm.target.legacy.enabled=false` unless they represent a service transaction that must be projected to a legacy transaction table.
- Audit events with `scm.audit.type=SERVICE` set `scm.target.legacy.enabled=false` by default unless explicitly required.
- Audit events with `scm.audit.type=CHANGE` set `scm.target.legacy.enabled=true` when they must be written to `cm.user_action_log`.

## Legacy Projection Routing

Supported legacy target tables:

- `ib.transaction`
- `rb.message_log`
- `cm.transaction_log`
- `cm.user_action_log`

Trace-based legacy projection rules:

| Condition | Target fields |
| --- | --- |
| `event.stream=trace` and the event represents an internet banking service transaction | `scm.target.legacy.enabled=true`, `scm.target.legacy.table=ib.transaction` |
| `event.stream=trace` and the event represents a mobile or PWA gateway service transaction | `scm.target.legacy.enabled=true`, `scm.target.legacy.table=rb.message_log` |
| `event.stream=trace` and the event represents a CM service transaction | `scm.target.legacy.enabled=true`, `scm.target.legacy.table=cm.transaction_log` |

Audit-based legacy projection rules:

| Condition | Target fields |
| --- | --- |
| `event.stream=audit` and `scm.audit.type=CHANGE` | `scm.target.legacy.enabled=true`, `scm.target.legacy.table=cm.user_action_log` |

If a producer cannot infer the target table unambiguously, the producing application must set the final `scm.target.legacy.table` explicitly.

## Mandatory Common Fields

These fields are mandatory for every JSONL event in every JSONL stream:

| Field | Type | Description |
| --- | --- | --- |
| `@timestamp` | date | Event timestamp. |
| `event.stream` | keyword | `log`, `trace`, or `audit`. |
| `event.kind` | keyword | Event kind, usually `event`. |
| `event.category` | keyword | High-level category such as `application`, `trace`, `audit`, `service`, or `transaction`. |
| `event.action` | keyword | Action name, for example `operation.call` or `service.audit`. |
| `event.outcome` | keyword | Event result. See outcome values below. |
| `scm.target.index` | keyword | Target index routing name. |
| `scm.target.legacy.enabled` | boolean | Whether legacy projection is enabled for this event. |
| `scm.platform` | keyword | `scm` or `cm`. |
| `service.name` | keyword | Emitting service/application name. |
| `deployment.environment` | keyword | Runtime environment, for example `prod`. |
| `scm.app.name` | keyword | Application name used in file path. |
| `scm.app.profile` | keyword | Application profile used in file path. |
| `scm.app.label` | keyword | Low-cardinality app label, for example `mobile`. |
| `scm.gateway.name` | keyword | Gateway name. |
| `scm.channel.code` | keyword | Channel code. |
| `scm.correlation_id` | keyword | Correlation ID shared across events for one request or transaction. |

## Mandatory Log Fields

Log events must include all common fields and:

| Field | Type | Description |
| --- | --- | --- |
| `log.level` | keyword | Log level such as `INFO`, `WARN`, or `ERROR`. |
| `logger.name` | keyword | Logger name. |
| `thread.name` | keyword | Thread name. |
| `message` | text | Log message. |

## Mandatory Trace Fields

Trace events must include all common fields and:

| Field | Type | Description |
| --- | --- | --- |
| `trace.id` | keyword | Trace identifier. |
| `span.id` | keyword | Span identifier. |
| `parent.span.id` | keyword | Parent span identifier. Empty string is allowed for a root span. |
| `span.name` | keyword | Canonical span name. |
| `span.kind` | keyword | Span kind such as `server`, `internal`, or `client`. |
| `span.start_time` | date | Span start time. |
| `span.end_time` | date | Span end time. |
| `span.duration_ms` | double | Span duration in milliseconds. |

Projected trace events must also include `scm.message.sequence_id`, `scm.status.code`, `scm.transaction.status`, and either `scm.transaction.type` or `scm.service.code`.

## Mandatory Audit Fields

Audit events must include all common fields and:

| Field | Type | Description |
| --- | --- | --- |
| `scm.audit.type` | keyword | Audit type. Allowed values are `SERVICE` and `CHANGE`. |
| `user.name` | keyword | User name or system principal. |

Audit type rules:

- `scm.audit.type=SERVICE` may be used for service/business audit events when needed.
- `scm.audit.type=CHANGE` is used for user, admin, configuration, and security change audit events.
- Legacy transaction table projection is trace-based and must not be driven by audit events.

## Service Transaction Fields

Service transaction context should include these fields when applicable:

| Field | Type | Description |
| --- | --- | --- |
| `scm.transaction.type` | keyword | Transaction type or business transaction name. |
| `scm.transaction.status` | keyword | Business transaction status. |
| `scm.message.sequence_id` | keyword | Message sequence ID. |
| `scm.status.code` | keyword | SCM status code. |
| `scm.csp.username` | keyword | CSP username from the business context. |
| `client.ip` | ip | Client IP address. |
| `scm.client.phone_number` | keyword | Client phone number. |
| `scm.access_parameter` | keyword | Access parameter used by the transaction. |
| `scm.amount` | double | Transaction amount. |
| `scm.card.no` | keyword | Card number field. |
| `scm.account.no` | keyword | Account number field. |
| `scm.destination` | keyword | Destination card, account, IBAN, or business destination. |
| `scm.legacy.source_table` | keyword | Source table for legacy compatibility. |

## Legacy Compatibility Fields

These fields preserve CM, IB, and Mobile gateway compatibility where the source data exists:

| Field | Type | Description |
| --- | --- | --- |
| `scm.legacy.source_table` | keyword | Legacy table that produced the event. |
| `scm.legacy.transaction_log_id` | keyword | Legacy transaction log ID. |
| `scm.legacy.message_log_id` | keyword | Legacy message log ID. |
| `scm.legacy.archive_no` | keyword | Legacy archive number. |
| `scm.legacy.eb_service_id` | keyword | Legacy EB service ID. |
| `scm.legacy.transaction_state_id` | keyword | Legacy transaction state ID. |
| `scm.external_sequence_id` | keyword | External sequence ID. |
| `scm.original_sequence_id` | keyword | Original sequence ID. |
| `scm.document.no` | keyword | Document number. |
| `scm.terminal.id` | keyword | Terminal ID. |
| `scm.terminal.type` | keyword | Terminal type. |
| `scm.transaction.inter_bank` | boolean | Whether the transaction is inter-bank. |
| `scm.transaction.duplicate` | boolean | Whether the transaction is a duplicate. |

## Security Rules

- Do not use masking fields in the contract.
- Card, account, and destination values may be stored with direct names: `scm.card.no`, `scm.account.no`, and `scm.destination`.
- Full JWT tokens must never be written to any observation output.
- Full Authorization headers must never be written.
- Do not write provider secrets, PIN, CVV2, MAC keys, PIN keys, or raw sensitive headers.
- If JWT information is needed later, only derived metadata is allowed:
  - `scm.auth.type`
  - `scm.auth.jwt.present`
  - `scm.auth.jwt.issuer`
  - `scm.auth.jwt.subject`
  - `scm.auth.jwt.username`
  - `scm.auth.jwt.exp`
  - `scm.auth.jwt.hash`

## Field Naming Conventions

- Use dot-separated lowercase names for SCM-specific fields under `scm.*`.
- Use common event names under `event.*`.
- Use standard names where they already exist, for example `trace.id`, `span.id`, `service.name`, `client.ip`, `log.level`, and `user.name`.
- Keep fields stable and low-cardinality unless they are business identifiers.
- Prefer explicit names over generic names, for example `scm.provider.code` instead of `provider`.
- Use snake_case for metric labels to match Prometheus conventions.

## Span Naming Conventions

Canonical span names:

- `gateway.receive`
- `service.execute`
- `operation.call`

Rules:

- Use canonical names for cross-platform trace compatibility.
- Put detailed business identity in attributes such as `scm.service.code`, `scm.operation.name`, and `scm.provider.code`.
- In the initial SCM Java 21 model, provider execution is not emitted as a separate `provider.call` span.
- Provider details and provider timing are recorded as attributes on the same `operation.call` span.
- Do not encode card number, account number, customer identifier, or provider secrets in `span.name`.

## Event Outcome Values

Allowed values:

| Value | Meaning |
| --- | --- |
| `success` | Completed successfully. |
| `failure` | Failed. |
| `unknown` | Outcome is not known at emission time. |
| `partial` | Partially completed. Use only when the business flow explicitly supports partial completion. |

## SCM Example

```json
{"@timestamp":"2026-06-10T14:15:30.310Z","event.stream":"trace","event.kind":"event","event.category":"transaction","event.action":"operation.call","event.outcome":"success","scm.target.index":"trace-scm-mobile-prod-2026.06.10.14","scm.target.legacy.enabled":true,"scm.target.legacy.table":"rb.message_log","scm.platform":"scm","service.name":"scm-web","deployment.environment":"prod","scm.app.name":"scm-web","scm.app.profile":"prod","scm.app.label":"mobile","scm.gateway.name":"mobile","scm.channel.code":"mobile","scm.correlation_id":"corr-20260610-0001","trace.id":"4d2f8a6b0e7c4a1db8f4d2f1a9c00101","span.id":"2f54b2a7c9e40003","parent.span.id":"1d8c0b7a44ef0002","span.name":"operation.call","span.kind":"client","span.start_time":"2026-06-10T14:15:30.221Z","span.end_time":"2026-06-10T14:15:30.310Z","span.duration_ms":89,"scm.service.code":"cardInquiry","scm.transaction.type":"CARD_INQUIRY","scm.transaction.status":"DONE","scm.message.sequence_id":"SCM-SEQ-0001","scm.status.code":"00"}
```

## CM Example

```json
{"@timestamp":"2026-06-10T14:18:12.390Z","event.stream":"trace","event.kind":"event","event.category":"transaction","event.action":"operation.call","event.outcome":"success","scm.target.index":"trace-cm-mobile-prod-2026.06.10.14","scm.target.legacy.enabled":true,"scm.target.legacy.table":"cm.transaction_log","scm.platform":"cm","service.name":"cm","deployment.environment":"prod","scm.app.name":"cm","scm.app.profile":"prod","scm.app.label":"mobile","scm.gateway.name":"mobile-gateway","scm.channel.code":"mobile","scm.correlation_id":"cm-corr-20260610-7788","trace.id":"8e2f8a6b0e7c4a1db8f4d2f1a9c00778","span.id":"cm-op-7788","parent.span.id":"cm-service-7788","span.name":"operation.call","span.kind":"client","span.start_time":"2026-06-10T14:18:12.300Z","span.end_time":"2026-06-10T14:18:12.390Z","span.duration_ms":90,"scm.service.code":"CARD_INQUIRY","scm.transaction.type":"CARD_INQUIRY","scm.transaction.status":"DONE","scm.message.sequence_id":"CM-SEQ-7788","scm.status.code":"00","scm.legacy.transaction_log_id":"984512"}
```
