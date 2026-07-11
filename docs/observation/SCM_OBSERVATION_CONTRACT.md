# SCM Observation Contract

SCM observability writes LOG, TRACE, and AUDIT as JSONL files for Filebeat. Metrics stay on the Actuator and Micrometer path.

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

Host applications configure only the base identity pattern:

```yaml
scm:
  observation:
    file:
      root-directory: ${SCM_OBS_ROOT_DIR:${user.home}/scm/obs}
      base-name-pattern: scm-${spring.application.name}-${spring.profiles.active}-${scm.metadata.namespace}-${scm.metadata.instance-id}
```

The starter owns stream prefix, hour token, roll index, and extension:

```text
{stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}-{rollIndex}.jsonl
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
```

File names never include channel code or Config label. Observation files are not gzipped and do not use a separate archive directory. Current and rolled files use final names while Filebeat reads them.

## Legacy Projection

Legacy database projection remains separate from the runtime metadata and file/index naming contract. When `scm.observation.legacy.enabled=true`, both of these fields must be present:

```text
scm.observation.legacy.service.code
scm.observation.legacy.operation.code
```

Host modules decide where legacy projection is appropriate. The starter does not infer legacy service or operation codes from span names.
