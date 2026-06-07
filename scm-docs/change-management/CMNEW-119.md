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
- `GatewayChannelLayerRouteBuilder` is now protocol/client-contract focused.
- `ServiceLayerRouteBuilder` builds direct service routes.
- `OperationLayerRouteBuilder` builds direct operation routes only for operations required by active runtime service plans.
- `GatewayRoutePipelineConfigurer` configures the internal gateway route pipeline and is not a top-level Camel `RouteBuilder`.
- `ServiceTargetRouter` owns `FIRST`, `FAIL_OVER` and `MULTI_OPERATION` target selection.
- `RuntimeRoutePlanProvider` loads runtime service plans for `channel.*` and `domain.*`.

Database:
- Existing v8 records remain unchanged.
- v9 uses new `GatewayChannel.name` values: `channel.*` and `domain.*`.
- v9 requires removing the old unique constraint on `CHANNEL_ID + PROTOCOL_TYPE`.
- v9 channel-service definitions must allow multiple `INBOUND` rows for the same channel/service/gateway when they point at different definitions.
- `GatewayChannel.name` remains the unique runtime key.
- `SVC_DOMAIN_MEMBER` is the final domain-membership enum name because of database size constraints.
- The v9 Java `ChannelServiceDefinitionType` enum no longer contains the old `REST`, `REST_MULTIPLE` or `SWAGGER` values.
- The v9 Java enum also no longer contains the intermediate route/group/API-documentation names from earlier CMNEW-119 drafts.

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
- Gateway and service logs include searchable fields where available: `traceId`, `spanId`, `correlationId`, `gatewayName`, `channelCode`, `serviceCode`, `serviceVersion`, `operationName`, `routeId`, `exchangeId`.
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
- Every active service plan must have at least one `INBOUND` definition. `API_DOC` is optional metadata.

`domain.*`:
- Load definitions from `TBL_SCM_CHN_SVC_DEFINITION` for the selected `GatewayChannel`.
- Only `SVC_DOMAIN_MEMBER` definitions create domain membership.
- `INBOUND` and `API_DOC` never create domain membership.
- Domain service routes are unique by `Service`; multiple member channels for the same service are collapsed into one service route and preserved as membership metadata.
- If a domain runtime has no `SVC_DOMAIN_MEMBER` definitions, startup fails fast.
- Every active domain member service must also have at least one `INBOUND` definition to become a runtime service plan. Members without `INBOUND` are skipped with a warning. `SVC_DOMAIN_MEMBER` is membership only and `API_DOC` does not expose a route.
- Multiple gateway routes are modeled as multiple `INBOUND` definitions. There is no group definition in the v9 runtime model.

Runtime guards normalize channel codes before comparison:
- incoming exchange/header channel code is trimmed and lower-cased with `Locale.ROOT`
- `scm.runtime.channel-affinity.allowed-channel-codes` values are normalized the same way
- `CHANNEL_SERVICE_ACCESS.channel.code` is normalized before access checks

Invalid `GatewayChannel.name` values fail fast. Protocol is always read from `GatewayChannel.protocolType`, never inferred from name.

## Enum Note

Historical v8 database values:
- `REST`
- `REST_MULTIPLE`
- `SWAGGER`

Final v9 Java enum values:
- `INBOUND`
- `API_DOC`
- `SVC_DOMAIN_MEMBER`

Historical mapping only:
- `REST -> INBOUND`
- `REST_MULTIPLE -> historical multi-route container only; v9 uses multiple INBOUND rows`
- `SWAGGER -> API_DOC`

Old v8 DB rows may still contain historical values, but v9 `channel.*` and `domain.*` runtimes must use new records with the final purpose-based enum values. The v9 Java enum does not keep the old values.

## Client Contract

Client contract is resolved per inbound route from `Definition.details.contract` and the path-based Client Contract Version.

Contract ownership:
- `INBOUND` can define client contracts.
- `SVC_DOMAIN_MEMBER` is membership metadata only. Any `contract` stored there is ignored and logged as a warning. Contracts under `API_DOC` are also ignored with a warning.
- Current request/response contract encoders are REST-only. SOAP/TCP require protocol-specific contract decoders/encoders before they can use `GLOBAL_RESPONSE_HANDLER`.

Versioning:
- `Definition.details.version` defines the external Client Contract Version when present and valid.
- If `version` is missing, a path that starts with `/vN/` resolves to `vN`.
- A path without a version segment resolves to `v1`.
- `/card/inquiry` and `/v1/card/inquiry` are both `v1`; `/v2/card/inquiry` is `v2`.
- `v1` can represent old CM-compatible client behavior: same URL, payloads, error format and HTTP status behavior where applicable.
- `ContractStyle` is intentionally not part of SCM. SCM should not know whether a client is legacy or modern.
- `versionSelector` is intentionally not required in this path-based phase.
- Gateway `routeId` values use compact layer prefixes and include the version, for example `gw.dm.domain-card.card-inquiry.v1` and `gw.dm.domain-card.card-inquiry.v2`.
- Service route IDs use `svc.<targetKindShort>.<gatewayName>.<serviceCode>`, for example `svc.dm.domain-card.card-inquiry`.
- Operation route IDs use `op.<operationName>`, while operation dispatch remains `direct:<operationName>`.
- Service route URIs remain version-agnostic by default, for example both v1 and v2 dispatch to the same service route unless business/provider behavior truly differs.

Fallback:
1. Route definition contract.
2. Gateway/protocol default.
3. Fail fast when no default exists.

Example v1 REST contract:

```json
{
  "version": "v1",
  "method": "POST",
  "path": "/card/inquiry",
  "contract": {
    "name": "card-inquiry-v1",
    "requestDecoder": "cardInquiryV1RequestDecoder",
    "responseEncoder": "cardInquiryV1ResponseEncoder",
    "faultEncoder": "cardInquiryV1FaultEncoder"
  }
}
```

Example v2 REST contract:

```json
{
  "version": "v2",
  "method": "POST",
  "path": "/v2/card/inquiry",
  "contract": {
    "name": "card-inquiry-v2",
    "requestDecoder": "cardInquiryV2RequestDecoder",
    "responseEncoder": "cardInquiryV2ResponseEncoder",
    "faultEncoder": "cardInquiryV2FaultEncoder"
  }
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
- `serviceVersion`
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
serviceCode:"card" AND serviceVersion:"v2"
channelCode:"mb" AND correlationId:"<correlation-id>"
```

## Deployment Order

1. Deploy database change that removes the old `CHANNEL_ID + PROTOCOL_TYPE` uniqueness, allows longer definition type values and allows multiple `INBOUND` rows for one channel/service/gateway when definitions differ.
2. Add v9 `GatewayChannel` records named `channel.*` or `domain.*`.
3. Add v9 `TBL_SCM_CHN_SVC_DEFINITION` rows with purpose-based definition types. Use `SVC_DOMAIN_MEMBER` only for domain membership.
4. Add route contracts and `version` in `Definition.details` on `INBOUND` where client contract versions differ.
5. Configure audit output path and Filebeat or Elastic Agent collection.
6. Deploy application.
7. Smoke test gateway routes by channel and domain runtime names.

## Validation and Smoke Tests

- Start app with `scm.runtime.gateway-name=channel.mb`; verify channel services without `INBOUND` or `API_DOC` fail fast and valid services create routes from `INBOUND`.
- Start app with `scm.runtime.gateway-name=domain.card`; verify only services with `SVC_DOMAIN_MEMBER`, `INBOUND` and `API_DOC` definitions are routed.
- Verify services with only `API_DOC` fail startup validation because `INBOUND` is required.
- Verify the legacy fallback still works with `scm.app-name=channel.mb` until config migration is complete.
- Call `/card/inquiry` and verify it uses the v1 client contract.
- Call `/v2/card/inquiry` and verify it uses the v2 client contract while dispatching to the same service route by default.
- Verify request channel is taken from the exchange/header and the service route resolves `CHANNEL_SERVICE_ACCESS` dynamically.
- Verify service plugins run in the service route, not in the gateway route.
- Verify audit JSON Lines contain `traceId`, `spanId` and `serviceVersion`, including a final `phase=SERVICE` success/failure event.
- Verify logs can be searched in Elastic by `traceId`, `spanId` and `serviceVersion`.
- Verify metrics still use Actuator/Micrometer flow and no metric file is created.

## Rollback Plan

1. Repoint `scm.runtime.gateway-name` or legacy `scm.app-name` to the previous v8 gateway runtime.
2. Disable new v9 `channel.*` or `domain.*` records without deleting v8 records.
3. Keep audit files for investigation; do not replay them into application state.
4. Roll back the application artifact.
5. Keep the database uniqueness change unless rollback policy requires strict v8 schema restoration.

## Assumptions

- Exact old CM-compatible payload/frame formats are not fully defined in this branch; v1 contract encoders preserve the response envelope extension point.
- Micrometer-specific service plugin metrics can be wired later behind `ServicePluginMetrics`.
- Provider routes do not need changes for this feature unless a provider-specific command/config is introduced by a concrete service rollout.
