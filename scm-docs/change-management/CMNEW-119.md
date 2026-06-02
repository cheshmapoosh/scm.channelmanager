# CMNEW-119 Gateway and Service Runtime Refactor

## Feature Summary

Refactors SCM runtime routing so the Gateway Layer only owns protocol/client exposure and the Service Layer owns service guards, service plugins, metrics, audit hooks and operation/provider target routing.

Gateway routes now dispatch to explicit service routes with:

```text
direct:scm.service.<normalized-service-code>
```

## Branch

`features/CMNEW-119`

## Target Version

`9.x.x`

## Impact Areas

Application:
- `GatewayChannelRouteBuilder` is now protocol/client-contract focused.
- `ServiceLayerRouteBuilder` builds direct service routes.
- `ServiceTargetRouter` owns `FIRST`, `FAIL_OVER` and `MULTI_OPERATION` target selection.
- `RuntimeRoutePlanProvider` loads runtime service plans for `channel.*` and `domain.*`.

Database:
- Existing v8 records remain unchanged.
- v9 uses new `GatewayChannel.name` values: `channel.*` and `domain.*`.
- v9 requires removing the old unique constraint on `CHANNEL_ID + PROTOCOL_TYPE`.
- `GatewayChannel.name` remains the unique runtime key.
- `ChannelServiceDefinitionType` column length must allow v9 values such as `SERVICE_DOMAIN_MEMBER`.

Config Server:
- Preferred runtime key is `scm.runtime.gateway-name`, for example `channel.mb` or `domain.card`.
- `scm.app-name` remains as a legacy fallback only and should be retired from new config.
- Optional runtime channel affinity:

```yaml
scm:
  runtime:
    gateway-name: channel.mb
    channel-affinity:
      enabled: false
      allowed-channel-codes:
        - "*"
```

Provider:
- Provider and operation routes are intentionally preserved.
- One minimal operation-layer change was required: when an operation route is invoked from the new service layer, operation exceptions are mapped to `ScmFault` and returned to the service route instead of being formatted into a protocol response. Direct legacy operation behavior still uses `GLOBAL_ERROR_HANDLER`.
- If a provider requires new commands, mappings, endpoints or config for a v9 service, document it in the provider module change note before deployment.

Logging/Elastic:
- Gateway and service logs include searchable fields where available: `traceId`, `spanId`, `correlationId`, `gatewayName`, `channelCode`, `serviceCode`, `operationName`, `routeId`, `exchangeId`.
- `TraceUtils` now places both `traceId` and `spanId` into MDC.

Audit:
- Service audit is available as a service plugin through `auditPluginHandler`.
- The service route also writes a final `SERVICE` audit event for overall service success or failure.
- Default audit file path is configurable with `scm.audit.output-path`.
- Audit output is JSON Lines and is suitable for Filebeat or Elastic Agent collection.
- Sensitive payloads are not written by default.

Metrics:
- Service plugin execution and service execution metrics are routed through `ServicePluginMetrics`.
- Current default implementation is a no-op internal abstraction so Micrometer wiring can be added without changing route code.
- Metrics are not written to files.

## Runtime Loading Rules

`channel.*`:
- Load services from `GatewayChannel.channel -> CHANNEL_SERVICE_ACCESS -> Service`.
- Filter inactive channel-service access records and unpublished services.
- This represents all services available to the channel.

`domain.*`:
- Load definitions from `TBL_SCM_CHN_SVC_DEFINITION` for the selected `GatewayChannel`.
- Only `SERVICE_DOMAIN_MEMBER` definitions create domain membership.
- `INBOUND_ROUTE`, `INBOUND_ROUTE_GROUP` and `API_DOCUMENTATION` never create domain membership.
- Domain service routes are unique by `Service`; multiple member channels for the same service are collapsed into one service route and preserved as membership metadata.
- If a domain runtime has no `SERVICE_DOMAIN_MEMBER` definitions, startup fails fast.
- Every active domain member service must also have `INBOUND_ROUTE` or `INBOUND_ROUTE_GROUP` exposure. `SERVICE_DOMAIN_MEMBER` is membership only and `API_DOCUMENTATION` does not expose a route.
- `INBOUND_ROUTE` and `INBOUND_ROUTE_GROUP` definitions may still define inbound REST routes and contracts for a member service.

Runtime guards normalize channel codes before comparison:
- incoming exchange/header channel code is trimmed and lower-cased with `Locale.ROOT`
- `scm.runtime.channel-affinity.allowed-channel-codes` values are normalized the same way
- `CHANNEL_SERVICE_ACCESS.channel.code` is normalized before access checks

Invalid `GatewayChannel.name` values fail fast. Protocol is always read from `GatewayChannel.protocolType`, never inferred from name.

## Enum Note

v8 values:
- `REST`
- `REST_MULTIPLE`
- `SWAGGER`

v9 values:
- `INBOUND_ROUTE`
- `INBOUND_ROUTE_GROUP`
- `API_DOCUMENTATION`
- `SERVICE_DOMAIN_MEMBER`

Compatibility mapping:
- `REST -> INBOUND_ROUTE`
- `REST_MULTIPLE -> INBOUND_ROUTE_GROUP`
- `SWAGGER -> API_DOCUMENTATION`

Legacy enum values remain temporarily in Java for v8 compatibility. v9 `channel.*` and `domain.*` runtimes should use the purpose-based v9 values.

## Client Contract

Client contract is resolved per inbound route from `Definition.details.contract`.

Contract ownership:
- `INBOUND_ROUTE` and `INBOUND_ROUTE_GROUP` can define client contracts.
- `SERVICE_DOMAIN_MEMBER` is membership metadata only. Any `contract` stored there is ignored and logged as a warning.
- Current request/response contract encoders are REST-only. SOAP/TCP require protocol-specific contract decoders/encoders before they can use `GLOBAL_RESPONSE_HANDLER`.

Fallback:
1. Route definition contract.
2. Gateway/protocol default.
3. Fail fast when no default exists.

Default REST contract:

```json
{
  "name": "modern-rest-v1",
  "requestDecoder": "jsonScmRequestDecoder",
  "responseEncoder": "jsonScmResponseEncoder",
  "faultEncoder": "restProblemDetailFaultEncoder"
}
```

Legacy REST example:

```json
{
  "name": "legacy-mb-card-v1",
  "requestDecoder": "legacyMbCardRequestDecoder",
  "responseEncoder": "legacyMbCardResponseEncoder",
  "faultEncoder": "legacyMbCardFaultEncoder"
}
```

## Audit Record Fields

Audit records contain:
- `timestamp`
- `traceId`
- `spanId`
- `correlationId`
- `gatewayName`
- `channelCode`
- `serviceCode`
- `operationName`
- `phase`
- `status`
- `errorCode`
- `errorMessage`
- `routeId`
- `exchangeId`

`phase=SERVICE` records describe the final service outcome. Plugin audit records still describe plugin execution points and must not be treated as final service success.

## Elastic Search Guidance

Find application logs and audit records by trace:

```text
traceId:"<trace-id>"
```

Find application logs and audit records by span:

```text
spanId:"<span-id>"
```

Common filters:

```text
gatewayName:"channel.mb" AND serviceCode:"card"
channelCode:"mb" AND correlationId:"<correlation-id>"
```

## Deployment Order

1. Deploy database change that removes the old `CHANNEL_ID + PROTOCOL_TYPE` uniqueness and allows longer definition type values.
2. Add v9 `GatewayChannel` records named `channel.*` or `domain.*`.
3. Add v9 `TBL_SCM_CHN_SVC_DEFINITION` rows with purpose-based definition types. Use `SERVICE_DOMAIN_MEMBER` only for domain membership.
4. Add route contracts in `Definition.details` on `INBOUND_ROUTE` or `INBOUND_ROUTE_GROUP` where legacy/modern behavior differs.
5. Configure audit output path and Filebeat or Elastic Agent collection.
6. Deploy application.
7. Smoke test gateway routes by channel and domain runtime names.

## Validation and Smoke Tests

- Start app with `scm.runtime.gateway-name=channel.mb`; verify a channel service route is created.
- Start app with `scm.runtime.gateway-name=domain.card`; verify only services with `SERVICE_DOMAIN_MEMBER` definitions and `INBOUND_ROUTE` or `INBOUND_ROUTE_GROUP` exposure are routed.
- Verify domain member services with only `API_DOCUMENTATION` fail startup validation.
- Verify the legacy fallback still works with `scm.app-name=channel.mb` until config migration is complete.
- Call a modern REST route and verify ProblemDetail faults.
- Call a legacy REST route and verify the legacy fault encoder path.
- Verify request channel is taken from the exchange/header and the service route resolves `CHANNEL_SERVICE_ACCESS` dynamically.
- Verify service plugins run in the service route, not in the gateway route.
- Verify audit JSON Lines contain `traceId` and `spanId`, including a final `phase=SERVICE` success/failure event.
- Verify logs can be searched in Elastic by `traceId` and `spanId`.
- Verify metrics still use Actuator/Micrometer flow and no metric file is created.

## Rollback Plan

1. Repoint `scm.runtime.gateway-name` or legacy `scm.app-name` to the previous v8 gateway runtime.
2. Disable new v9 `channel.*` or `domain.*` records without deleting v8 records.
3. Keep audit files for investigation; do not replay them into application state.
4. Roll back the application artifact.
5. Keep the database uniqueness change unless rollback policy requires strict v8 schema restoration.

## Assumptions

- Exact legacy payload/frame formats are not fully defined in this branch; placeholder legacy encoders preserve the existing response envelope extension point.
- Micrometer-specific service plugin metrics can be wired later behind `ServicePluginMetrics`.
- Provider routes do not need changes for this feature unless a provider-specific command/config is introduced by a concrete service rollout.
