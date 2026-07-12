# Observability - scm-web

## Scope and eligibility

`scm-web` emits business TRACE only after a request resolves to a real SCM Camel route. Spring startup, context and bean initialization, route registration, provider/cache initialization, health and readiness checks, actuator, admin/config, documentation, static resources, and ordinary unmapped HTTP requests remain application LOG activity and do not create business spans.

The module explicitly keeps the generic Servlet HTTP server span disabled:

```yaml
scm:
  observation:
    http:
      server:
        enabled: false
        mode: channel-only
```

`scm-observation-servlet-starter` may still provide correlation/MDC support. It does not own the SCM business trace.

## HTTP transport boundary

`HttpGatewayObservationFilter` prepares transport context only. It may accept a validated W3C `traceparent` trace identifier, generate local trace/correlation/span identifiers, set safe request attributes and MDC fields, and return `X-Correlation-Id`. It does not call the observation lifecycle, open `gateway.receive`, or create any replacement HTTP or gateway span.

The safe HTTP attributes are:

```text
http.method
url.path
http.query.present
client.ip
```

`client.ip` may use trusted transport forwarding metadata. It is distinct from the allowlisted JWT `acp` attribute `scm.client.address`; the HTTP filter never populates that JWT-owned field.

Because the filter does not create spans, requests that stop at Servlet handling—including ordinary 404 responses—cannot create business TRACE. Actuator, internal/admin/config, Swagger/OpenAPI/docs, and static-resource paths are also excluded from this filter.

## Business span hierarchy

A normal successful external business request has exactly three spans:

```text
gateway.receive                 (server, root)
└── service.execute             (internal)
    └── operation.call          (operation-specific kind)
```

Ownership and lifecycle:

| Route owner | Span | Start | Close |
| --- | --- | --- | --- |
| `GatewayChannelLayerRouteBuilder` pipeline | `gateway.receive` | after the inbound request resolves to the SCM gateway route | after gateway response/fault processing |
| `ServiceLayerRouteBuilder` | `service.execute` | immediately before service-layer execution | on the service success or failure path |
| `OperationLayerRouteBuilder` | `operation.call` | immediately before the selected operation call | on the operation success or failure path |

The spans share `trace.id` and `correlation.id`, use distinct `span.id` values, and carry these parent relationships:

```text
service.execute.parent.span.id   = gateway.receive.span.id
operation.call.parent.span.id    = service.execute.span.id
```

`gateway.request` and `gateway.response` are not spans. Safe request metadata is attached to `gateway.receive`; completion metadata may be recorded as the `gateway.response.completed` event in that span.

Active scopes are local runtime state held in Camel Exchange properties:

```text
scm.observation.scope.gateway
scm.observation.scope.service
scm.observation.scope.operation
```

This preserves layer ownership across Camel thread changes. Scope objects are not serialized into outbound messages.

## Plugin events

Plugins do not open `plugin.execute` spans. The core plugin invocation wrapper measures the actual invocation and records exactly one completion event named `plugin.execute` in the active Exchange-held scope for the executing layer.

Each event contains only registered safe metadata such as:

```text
plugin.name
plugin.type
plugin.phase
plugin.layer
plugin.duration_ms
event.outcome
error.type      (failure only)
error.code      (failure only)
```

The module-level `ScmWebPluginObservationListener` retains structured LOG behavior only; it is not a second TRACE-event source. Plugin inputs, outputs, credentials, account/card/OTP data, and unrestricted failure messages are excluded.

## Trace document and timing

TRACE documents use:

```text
event.stream=trace
event.action
event.outcome
```

They do not duplicate the stream as `event.category=trace`.

Every completed span contains `span.start_time`, `span.end_time`, and non-negative `span.duration_ms`. UTC `Instant` values supply the timestamps; monotonic `System.nanoTime()` elapsed time supplies the duration. `plugin.duration_ms` is measured separately around the actual plugin invocation and is not a span duration.

Collected events are emitted in insertion order under `span.events` in the single final span document.

## Validated JWT enrichment

JWT enrichment applies only to the open `gateway.receive` scope. `service.execute`, `operation.call`, plugin events, LOG, and AUDIT do not receive the claim set.

The resolver reads only an authenticated Spring Security result. It supports the standard `JwtAuthenticationToken`, the active SCM resource-server converter's authenticated `UsernamePasswordAuthenticationToken` carrying the already validated Spring `Jwt`, and the SCM `UserAuthentication` compatibility path whose authentication-manager result retains the validated `Jwt` in authentication details. It never reads an HTTP authentication header, splits or decodes a compact token, validates a token, or copies the full claim map.

After successful authentication, `ScmWebGatewayAuthenticationTraceEnricher` creates an immutable allowlisted `GatewayJwtTraceContext`. A non-empty context is stored locally on the Camel Exchange as:

```text
scm.observation.context.gateway.jwt
```

It then enriches the still-open Exchange-held gateway scope. Anonymous, absent, unauthenticated, or non-JWT authentication produces no JWT attributes and leaves no empty JWT context property.

The complete claim allowlist is:

| JWT claim | `gateway.receive` attribute | Type |
| --- | --- | --- |
| `sub` | `scm.user.nickname` | keyword/string |
| `scope` | `scm.jwt.scope` | keyword collection |
| `iss` | `scm.jwt.issuer` | keyword/string |
| `acp` | `scm.client.address` | keyword/string |
| `iat` | `scm.jwt.issue_at` | date |
| `exp` | `scm.jwt.expire_at` | date |
| `trm` | `scm.channel.code` | existing keyword/string |
| `aud` | `scm.jwt.audience` | keyword collection |
| `grn` | `scm.jwt.generator` | keyword/string |
| `tam` | `scm.auth.txn_method` | keyword/string |
| `lam` | `scm.auth.login_method` | keyword/string |

Normalization rules:

- `sub`, `iss`, `acp`, `trm`, `grn`, `tam`, and `lam` are trimmed strings; null and blank values are omitted.
- `scope` accepts a collection, array, space-delimited string, or single string. Values are trimmed, blanks removed, and duplicates removed while preserving order.
- `aud` accepts a collection, array, or single string and applies the same trim/blank/deduplication rules without whitespace expansion.
- `iat` and `exp` accept Spring `Instant` values or valid JWT NumericDate seconds and are emitted as ISO-8601 UTC timestamps. Invalid values are omitted without failing the request.
- A nonblank `trm` changes only the gateway span's `scm.channel.code` attribute. When absent, the existing route/gateway trace channel value remains. It does not change routing or channel-affinity decisions.

The raw JWT value, HTTP authentication header value, credentials, and claims outside this table are forbidden observation data. They are never stored in the immutable trace context or written to TRACE, LOG, or AUDIT.

Scheduled background work does not receive invented JWT or user attributes.

## Trace files

The active trace file is stable for both synchronous and asynchronous appenders:

```text
${TRACE_FILE_DIRECTORY}/trace-${OBS_FILE_BASE_NAME}.jsonl
```

For the standard local `scm-web` identity this resembles:

```text
trace-scm-scm-web-dev-local-local-scm-web.jsonl
```

Only rolled files contain the hour and rolling index:

```text
${TRACE_FILE_DIRECTORY}/trace-${OBS_FILE_BASE_NAME}-%d{yyyyMMdd-HH,...}-%i.jsonl
```

For example:

```text
trace-scm-scm-web-dev-local-local-scm-web-20260712-11-0.jsonl
```

The optional simple trace appender follows the same stable-active/dated-rolled split with a `.log` suffix. LOG and AUDIT naming is unchanged.

## Target routing, legacy projection, and metrics

Every observation record retains its required deployment and target-index metadata. `scm.channel.code` is a business attribute used in dynamic index routing and is not part of the physical filename.

Legacy projection remains opt-in for configured real business routes. Health, actuator, admin/config, docs/static, and internal endpoints are not legacy projection records.

Metrics continue through Actuator and Micrometer. They are not written to JSONL files and do not use the trace file path.
