# Observability - scm-web

## Scope

`scm-web` is the gateway runtime. The first runtime rollout is limited to gateway entry lifecycle observation.

The design is protocol-neutral in `scm-observation-starter`:

```text
GatewayObservationLifecycle
  -> ScmObservation.trace()
  -> ScmObservation.log()
  -> ScmObservation.metric()
```

HTTP is only the first adapter in this module:

```text
HTTP request -> HttpGatewayObservationFilter -> GatewayObservationLifecycle
```

Future adapters should reuse the same lifecycle:

```text
SOAP    -> future SoapGatewayObservationAdapter
MQ/JMS  -> future MqGatewayObservationAdapter
TCP/ISO -> future TcpGatewayObservationAdapter
```

Not implemented in this rollout:

```text
service.execute
operation.call
provider attributes
plugin observation
audit rollout
SOAP adapter
MQ/JMS adapter
TCP/ISO adapter
```

## Correlation

`HttpGatewayObservationFilter` uses `X-Correlation-Id` as the official HTTP correlation header.

Rules:

- If the incoming header is present and not blank, the same value is used.
- Otherwise a new correlation id is generated.
- The response always includes `X-Correlation-Id`.
- Request attributes include `scm.gateway.observation.context`, `scm.correlation_id`, `scm.trace.id`, `scm.gateway.span.id`, `scm.gateway.name`, and `scm.channel.code`.
- Distributed trace propagation uses W3C `traceparent`. Custom `X-SCM-*` trace headers are not the source of truth.

## Trace

The gateway entry span is:

```text
gateway.receive
```

Common protocol-neutral attributes include:

```text
scm.protocol
scm.request.name
scm.message.id
client.address
```

The HTTP adapter adds safe HTTP metadata only:

```text
http.method
url.path
http.query.present
http.status_code
client.ip
```

Request body, response body, Authorization, JWT, cookies, and raw query string are not captured.

## Log

Each gateway request emits one structured observation log event through the lifecycle:

```text
request.completed -> success
request.failed    -> failure
```

The log event includes correlation id, trace id, gateway span id, gateway/channel, protocol, request name, safe adapter attributes, and duration.

## Target Routing

Every LOG, TRACE, and AUDIT record includes:

```text
event.stream
scm.observation.target.namespace
scm.observation.target.index
scm.platform
service.name
deployment.environment
```

`scm.observation.target.index` is resolved dynamically from stream, namespace, environment, timestamp, and real business channel code when present. `scm.channel.code` remains a business attribute and is not used for physical file names.

Files are namespace-based:

```text
{stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}.jsonl
```

In Kubernetes, `SCM_OBSERVATION_TARGET_NAMESPACE` and `SCM_INSTANCE_ID` come from the Downward API. `SCM_OBS_NAMESPACE` is accepted only as a compatibility fallback.

## Legacy Projection

The main end-user gateway span is:

```text
span.name = gateway.receive
```

Only real end-user channel requests may set:

```text
scm.observation.legacy.enabled = true
scm.observation.legacy.operation.code
scm.observation.legacy.service.code
```

`scm.observation.legacy.service.code` must come from route/service configuration, not from `span.name`. Health checks, actuator, admin/config, static resource, docs, and internal endpoints are not legacy projection records.

Gateway channel and legacy mappings are configured through:

```yaml
scm:
  web:
    observation:
      gateway:
        channel:
          path-prefix-mappings: "/ib=ib,/mb=mb"
        legacy:
          route-mappings: "/ib/payments=PAYMENT:TRANSFER,/mb/cards=CARD:CARD_INQUIRY"
```

## Metric

Gateway metrics are emitted through `ScmObservation.metric()` and Micrometer when a `MeterRegistry` is available.

Current metric names:

```text
scm.gateway.requests
scm.request.duration
scm.faults
```

Metric tags are low-cardinality only: app, profile, label, platform, channel, gateway, protocol, request name, outcome, and error code when available.

Metrics do not write JSONL files and do not use `scm.observation.target.index`.
