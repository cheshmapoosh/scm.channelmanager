# Observability - scm-uaa

## Contract

Every LOG, TRACE, and AUDIT record includes:

```text
event.stream
scm.obs.target.namespace
scm.obs.target.index
scm.platform
service.name
deployment.environment
```

`scm.obs.target.index` is resolved dynamically from stream, namespace, environment, timestamp, and a real business `scm.channel.code` when present. Startup and platform logs normally do not have a channel code, so their index omits the channel segment.

Files are namespace-based, not channel-based:

```text
{stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}.jsonl
```

In Kubernetes, `SCM_OBS_NAMESPACE` and `SCM_INSTANCE_ID` come from pod metadata through the Downward API. Outside Kubernetes, namespace defaults to `default`.

Console output for LOG, TRACE, and AUDIT is enabled only in the `dev` profile. Test, pilot, and prod keep console disabled and file output enabled.

Distributed tracing uses W3C `traceparent` as the source of truth. Custom `X-SCM-*` trace headers are not used for distributed trace propagation.

## Trace

UAA traces focus on authentication and token flows. Sensitive values such as passwords, client secrets, access tokens, refresh tokens, authorization codes, and raw usernames must not be logged or traced.

The login entry span is:

```text
uaa.auth.login
```

Technical child spans should carry only safe metadata and should not be marked as legacy unless explicitly required later.

## Legacy Projection

The UAA business entry spans for these operations may set legacy projection:

```text
login
change-password
update-favorite-account
update-account-label
```

Those spans set:

```text
scm.obs.legacy.enabled = true
scm.obs.legacy.operation.code
scm.obs.legacy.service.code
```

Legacy service codes come from explicit configuration under `scm.uaa.observation.legacy.operations.*.service-code`. They are not extracted from `span.name`.

## Metric

Metrics are exported through Actuator, Micrometer, Prometheus, and Grafana. Metrics are not written to LOG, TRACE, or AUDIT files.
