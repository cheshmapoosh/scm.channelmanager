# scm-observation-starter

`scm-observation-starter` provides the shared SCM observation foundation for LOG, TRACE, AUDIT, and METRIC work. Adding the dependency alone enables nothing; every signal is opt-in and fail-closed.

## Architecture

```text
LOG    -> Logback JSONL -> Filebeat -> Elasticsearch -> Kibana
TRACE  -> starter event -> Logback routing marker -> JSONL -> Filebeat -> Elasticsearch -> Kibana
AUDIT  -> starter event -> Logback routing marker -> JSONL -> Filebeat -> Elasticsearch -> Kibana
METRIC -> Actuator -> Micrometer -> Prometheus -> Grafana
```

Metrics are not JSONL records. Host modules expose Actuator/Micrometer metrics and use starter-owned generic models where shared calculation logic is needed.

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

`ObservationAttributeSensitivity` is registry policy. `RAW` is emitted as-is, `SECURE` is replaced with `[SECURE]`, and prefix/suffix masking modes expose only configured visible characters.

## Responsibility Boundaries

`ObservationAttributeRegistry` is the attribute catalog. It owns metadata lookup, stream allow-list checks, blank/null/drop handling, and sensitivity masking through `prepareValue(...)`. It does not build observation documents, validate full records, or run regex/content secret scrubbing.

`ObservationDocumentFactory` builds base documents for `LOG`, `TRACE`, and `AUDIT`. It owns common field placement and default base values only. It does not own validation rules or duplicate `correlation.type` allowed values.

`ObservationRecordValidator` validates a finished document by `ObservationStream`, `ObservationRecordKind`, required `ObservationAttributePresence`, and the active `ObservationAttributeRegistry`. It does not build or sanitize documents.

`ObservationDocumentBuilder` is the assembly path used by LOG, TRACE, and AUDIT. It applies `SecretScrubbingObservationSanitizer` first for hard safety, then calls `ObservationAttributeRegistry.prepareValue(...)` for contract policy and masking.

`SecretScrubbingObservationSanitizer` is hard safety only. It redacts raw JWTs, Bearer tokens, Authorization values, invalid `scm.auth.jwt.hash` values, and obvious secret assignments inside free text, maps, collections, and arrays. The hard replacement token is always `[SECURE]`.

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
        .correlationType(CorrelationType.LIFECYCLE.value())
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

Allowed `correlation.type` values are defined by `CorrelationType`:

```text
LIFECYCLE -> lifecycle
REQUEST -> request
MESSAGE -> message
JOB -> job
BATCH -> batch
OPERATION -> operation
UNKNOWN -> unknown
```

Use `lifecycle` only for startup, shutdown, bootstrap, and runtime context creation. Missing context should be `unknown` or a clear generated fallback, not lifecycle.

The base document factory defaults a missing `correlation.type` to `CorrelationType.UNKNOWN`. Invalid non-empty values are rejected by `ObservationRecordValidator`.

## JSONL Providers

The bundled JSONL provider builds the full official LOG document from the Logback event, MDC, registered structured fields, marker kind, and throwable state. It uses `ObservationAttributeRegistryHolder`, drops unregistered fields, warns once for each unknown dropped field, runs `SecretScrubbingObservationSanitizer` for hard safety, then applies registry masking/sensitivity through `prepareValue(...)`.

TRACE and AUDIT documents are built through `ObservationDocumentFactory` and validated with `ObservationRecordValidator`. The legacy base document path was removed; new documents do not use the old stream, service, app, gateway, channel, correlation, or target projection fields.

Raw `StructuredArguments.kv(...)` is not the official contract path for junior developers. Use `ScmObservation` builders or official SCM log helpers so record kind, correlation type, validation, hard secret scrubbing, and registry masking are applied consistently.

## Common LOG Attributes

The source of truth for common LOG fields is:

```text
src/main/resources/META-INF/scm/docs/observation/common-log-attributes.md
```

Keep that file aligned with `ScmCommonLogAttributes`.

## Element Risk

`scm-observation-starter` owns the generic SCM element risk and health model. Host modules bind their own configuration prefix by extending `ScmElementRiskProperties`, then provide runtime samples and HealthIndicator adapters.

Risk is application-computed before metrics are exported. Grafana and Prometheus consume final values only:

```text
risk: 0=normal, 1=warning, 2=critical
health: 1=healthy, 0=unhealthy
```

`warningRatio` and `criticalRatio` are capacity ratios between `0` and `1`; count, millisecond, byte, latency, error, and memory thresholds must use separate future properties with explicit units. Grafana should alert on final risk and health metrics, not threshold math.
