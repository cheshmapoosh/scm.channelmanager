# scm-observation-starter

`scm-observation-starter` provides the shared SCM observation foundation for LOG, TRACE, AUDIT, and future METRIC work. Adding the dependency alone enables nothing; every signal is opt-in and fail-closed.

## Architecture

```text
LOG    -> Logback JSONL -> Filebeat -> Elasticsearch -> Kibana
TRACE  -> starter event -> Logback routing marker -> JSONL -> Filebeat -> Elasticsearch -> Kibana
AUDIT  -> starter event -> Logback routing marker -> JSONL -> Filebeat -> Elasticsearch -> Kibana
METRIC -> Actuator -> Micrometer -> Prometheus -> Grafana
```

Metrics are not JSONL records. Cycle 0 does not implement Actuator/Prometheus wiring or cache metrics.

## Core Concepts

`ObservationStream` identifies the output stream: `LOG`, `TRACE`, `AUDIT`, or future `METRIC`.

`ObservationRecordKind` is validation context only. It is never written to JSONL.

| kind | required presence |
| --- | --- |
| `PLAIN` | `ALWAYS_REQUIRED` |
| `CONTEXT` | `ALWAYS_REQUIRED`, `CONTEXT_REQUIRED` |
| `EVENT` | `ALWAYS_REQUIRED`, `EVENT_REQUIRED` |
| `EXCEPTION` | `ALWAYS_REQUIRED`, `ERROR_REQUIRED` |
| `CHANGE` | `ALWAYS_REQUIRED` |

When an `EVENT`, `CONTEXT`, or `CHANGE` record also has a throwable, `ERROR_REQUIRED` is added. `ERROR_REQUIRED` means a throwable, fault, or exception marker exists; it does not mean `log.level=ERROR`.

`ObservationAttributePresence` defines required and optional fields without boolean `required` flags. Use `ALWAYS_REQUIRED`, `CONTEXT_REQUIRED`, `CONTEXT_OPTIONAL`, `EVENT_REQUIRED`, `EVENT_OPTIONAL`, `ERROR_REQUIRED`, `ERROR_OPTIONAL`, or `ON_CHANGE_OPTIONAL`.

`ObservationAttributeSensitivity` controls value handling after sanitizer processing. `RAW` is emitted as-is, `SECURE` is replaced, and prefix/suffix masking modes expose only configured visible characters.

## Registry Model

`ObservationAttributeContributor` is Spring Bean based. Do not register contributors with Java SPI or `META-INF/services`.

Spring auto-configuration builds one `ObservationAttributeRegistry` from built-in starter attributes and Spring contributors. Duplicate metadata for the same stream and field must be compatible or startup fails.

`ObservationAttributeRegistryHolder` publishes the Spring-built registry for Logback providers. If Logback initializes before Spring publishes the registry, providers use the common-only fallback registry. There is no SPI fallback.

## Official LOG API

Junior developers should use either normal SLF4J messages or the official SCM observation API/helpers. Do not use raw `StructuredArguments.kv(...)` as the official path; unregistered fields are dropped by the JSONL provider and warned once per field name.

```java
observation.log()
        .event()
        .category("cache.init")
        .action("hazelcast.bootstrap.started")
        .outcome("success")
        .correlationId(correlationId)
        .correlationType("lifecycle")
        .message("Hazelcast bootstrap started")
        .write();
```

Supported LOG builder flows:

- `plain()`
- `context()`
- `event()`
- `exception(Throwable)`
- `change()`
- `category(String)`
- `action(String)`
- `outcome(String)`
- `correlationId(String)`
- `correlationType(String)`
- `message(String)`
- `write()`

## SLF4J Markers

RecordKind markers are for normal LOG JSONL validation:

| marker | record kind |
| --- | --- |
| `SCM_CONTEXT` | `CONTEXT` |
| `SCM_EVENT` | `EVENT` |
| `SCM_EXCEPTION` | `EXCEPTION` |
| `SCM_CHANGE` | `CHANGE` |

Detection rules:

- `log.info("x")`, `log.warn("x")`, and `log.error("x")` without marker and without throwable are `PLAIN`.
- `log.warn("x", throwable)` and `log.error("x", throwable)` are `EXCEPTION`.
- `log.error("x")` without throwable does not require `error.*`.
- Marker plus throwable validates both marker-required fields and `ERROR_REQUIRED`.

Routing markers are not RecordKind markers:

```text
SCM_OBSERVATION_TRACE
SCM_OBSERVATION_AUDIT
```

They route starter TRACE/AUDIT payloads to dedicated JSONL appenders and must stay out of normal application appenders.

## Correlation

Allowed `correlation.type` values:

```text
lifecycle
request
message
job
batch
operation
unknown
```

Use `lifecycle` only for startup, shutdown, bootstrap, and runtime context creation. Missing context should be `unknown` or a clear generated fallback, not lifecycle.

## JSONL Providers

The bundled JSONL provider builds the full official LOG document from the Logback event, MDC, registered structured fields, marker kind, and throwable state. It uses `ObservationAttributeRegistryHolder`, drops unregistered fields, warns once for each unknown dropped field, applies the sanitizer, and applies registry masking/sensitivity.

TRACE and AUDIT documents are built through `ObservationDocumentFactory` and validated with `ObservationRecordValidator`. The legacy base document path was removed; new documents do not use the old stream, service, app, gateway, channel, correlation, or target projection fields.

## Common LOG Attributes

The source of truth for common LOG fields is:

```text
src/main/resources/META-INF/scm/docs/observation/common-log-attributes.md
```

Keep that file aligned with `ScmCommonLogAttributes`.
