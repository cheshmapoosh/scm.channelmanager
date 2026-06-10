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
legacy table projection
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

## Metric

Gateway metrics are emitted through `ScmObservation.metric()` and Micrometer when a `MeterRegistry` is available.

Current metric names:

```text
scm.gateway.requests
scm.request.duration
scm.faults
```

Metric tags are low-cardinality only: app, profile, label, platform, channel, gateway, protocol, request name, outcome, and error code when available.

Metrics do not write JSONL files and do not use `scm.target.index`.
