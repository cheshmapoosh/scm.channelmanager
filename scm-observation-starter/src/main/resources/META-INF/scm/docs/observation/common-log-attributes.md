# Common LOG Attributes

`correlation.type` values are defined by `CorrelationType`: `lifecycle`, `request`, `message`, `job`, `batch`, `operation`, and `unknown`.
Use `lifecycle` only for startup, shutdown, bootstrap, and runtime context creation. Missing context defaults to `unknown`.

| name | Java type | Elasticsearch type | owner | streams | presence | sensitivity | visiblePrefixLength | visibleSuffixLength | description |
| --- | --- | --- | --- | --- | --- | --- | ---: | ---: | --- |
| `@timestamp` | String | date | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Log event timestamp. |
| `log.level` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Log severity level. |
| `log.logger` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Logger name. |
| `process.thread.name` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Thread name that emitted the log. |
| `message` | String | text | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Rendered log message. |
| `event.stream` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Observation stream: log, trace, or audit. |
| `scm.metadata.namespace` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | SCM runtime metadata namespace. |
| `scm.metadata.instance_id` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | SCM runtime metadata instance id. |
| `scm.metadata.time_zone` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | SCM runtime metadata timezone. |
| `scm.config.label` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Spring Cloud Config label. |
| `scm.observation.target.index` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Final Elasticsearch routing index. |
| `service.name` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Service name used by observability backends. |
| `deployment.service.name` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Running SCM service name. |
| `deployment.service.version` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Running SCM service version. |
| `deployment.environment` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Deployment environment: dev, test, pilot or prod. |
| `scm.runtime` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Runtime mode: standalone or kubernetes. |
| `scm.channel.code` | String | keyword | common | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Business channel code. It is not used for physical file routing. |
| `scm.observation.legacy.enabled` | Boolean | boolean | common | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Whether this record is projected to a legacy business view. |
| `scm.observation.legacy.service.code` | String | keyword | common | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Explicit legacy service code. Required when legacy projection is enabled. |
| `scm.observation.legacy.operation.code` | String | keyword | common | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Explicit legacy operation code. Required when legacy projection is enabled. |
| `correlation.id` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Correlation id for the active CorrelationType context. |
| `correlation.type` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Correlation type from the CorrelationType enum. |
| `event.category` | String | keyword | common | LOG | EVENT_REQUIRED | RAW | 0 | 0 | SCM event category. |
| `event.action` | String | keyword | common | LOG | EVENT_REQUIRED | RAW | 0 | 0 | SCM event action. |
| `event.outcome` | String | keyword | common | LOG | EVENT_REQUIRED | RAW | 0 | 0 | SCM event outcome: success, failure, unknown or skipped. |
| `trace.id` | String | keyword | common | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Tracing id when a real tracing context exists. |
| `span.id` | String | keyword | common | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Span id when a real tracing context exists. |
| `error.type` | String | keyword | common | LOG | ERROR_REQUIRED | RAW | 0 | 0 | Throwable or fault type. |
| `error.message` | String | text | common | LOG | ERROR_OPTIONAL | RAW | 0 | 0 | Throwable or fault message. |
| `error.stack_trace` | String | text | common | LOG | ERROR_OPTIONAL | RAW | 0 | 0 | Throwable stack trace. |
| `error.code` | String | keyword | common | LOG | ERROR_OPTIONAL | RAW | 0 | 0 | Application error code. |
| `error.category` | String | keyword | common | LOG | ERROR_OPTIONAL | RAW | 0 | 0 | Application error category. |
