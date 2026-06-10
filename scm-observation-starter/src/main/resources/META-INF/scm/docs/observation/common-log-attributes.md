# Common LOG Attributes

| name | Java type | Elasticsearch type | owner | streams | presence | sensitivity | visiblePrefixLength | visibleSuffixLength | description |
| --- | --- | --- | --- | --- | --- | --- | ---: | ---: | --- |
| `@timestamp` | String | date | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Log event timestamp. |
| `log.level` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Log severity level. |
| `log.logger` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Logger name. |
| `process.thread.name` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Thread name that emitted the log. |
| `message` | String | text | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Rendered log message. |
| `correlation.id` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Correlation id for lifecycle, request, message, job, batch or operation logs. |
| `correlation.type` | String | keyword | common | LOG | ALWAYS_REQUIRED | RAW | 0 | 0 | Correlation type: lifecycle, request, message, job, batch or operation. |
| `deployment.service.name` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Running SCM service name. |
| `deployment.service.version` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Running SCM service version. |
| `deployment.environment` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Deployment environment: dev, test, pilot or prod. |
| `scm.runtime` | String | keyword | common | LOG | CONTEXT_REQUIRED | RAW | 0 | 0 | Runtime mode: standalone or kubernetes. |
| `container.image.name` | String | keyword | common | LOG | CONTEXT_OPTIONAL | RAW | 0 | 0 | Injected container image name. |
| `container.image.tag` | String | keyword | common | LOG | CONTEXT_OPTIONAL | RAW | 0 | 0 | Injected container image tag. |
| `kubernetes.namespace` | String | keyword | common | LOG | CONTEXT_OPTIONAL | RAW | 0 | 0 | Kubernetes namespace. |
| `kubernetes.pod.name` | String | keyword | common | LOG | CONTEXT_OPTIONAL | RAW | 0 | 0 | Kubernetes pod name. |
| `kubernetes.node.name` | String | keyword | common | LOG | CONTEXT_OPTIONAL | RAW | 0 | 0 | Kubernetes node name. |
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
