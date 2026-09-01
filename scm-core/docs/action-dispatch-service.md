# Defining ACTION_DISPATCH services

## Purpose and semantics

`ACTION_DISPATCH` is the stateless service-routing strategy for services that
offer several logical actions. A request selects one configured action, that
action selects one active Operation, and SCM executes that Operation exactly
once through its existing provider route.

Version 1 deliberately supports only this shape:

```text
one canonical action -> one ServiceOperation -> one Operation -> its Provider
```

It does not provide aliases, workflows, durable state, cursors, locking,
automatic failover, or server-side retry. Two actions cannot reference the same
Operation. A caller may retry a complete request only according to the selected
action's own idempotency contract.

## Ownership boundaries

```text
Gateway path + method
  -> configured inboundAction
  -> matching service-operation Definition
  -> associated ServiceOperation
  -> selected Operation
  -> Operation's existing Provider
```

- Gateway owns the external path/method to `inboundAction` mapping. It does not
  name an Operation.
- Definition owns `inboundAction` routing metadata.
- ServiceOperation owns executable `operationName` configuration and uses its
  existing Definition relationship; it owns no routing selector.
- Operation owns plugins, path, type, provider identity, and provider lifecycle.
- Provider owns invocation of its target. `ACTION_DISPATCH` does not change
  REST or internal `scm:` provider behavior.

Service-level before/after plugins remain outside the dispatch processor and
therefore run once per request. Operation-level plugins remain on the selected
Operation route.

## Configuration model

Set the Service routing strategy to the persisted enum name:

```text
Service:
  code            = OTP
  routingStrategy = ACTION_DISPATCH
```

Create one active service-operation binding per action:

```text
ServiceOperation:
  service       = OTP
  active        = true
  operationName = UAA_OTP_VERIFY
  definition    = OTP_VERIFY

ServiceOperation:
  service       = OTP
  active        = true
  operationName = UAA_OTP_RESEND
  definition    = OTP_RESEND
```

Each referenced Definition supplies the selector in `details`:

```json
{
  "inboundAction": "otp.verify"
}
```

This is the same ownership pattern used by TASK_WORKFLOW: an active
ServiceOperation associates a Definition with a runtime target, and
`Definition.details.inboundAction` supplies the external routing identity.

An `INBOUND` definition keeps `method` and `path` in their existing first-class
fields. Its `Definition.details` supplies the fixed action:

```json
{
  "inboundAction": "otp.verify"
}
```

For example, an `INBOUND` row with `method=POST`, `path=/otp/verify`, and the
details above binds that route to `otp.verify`. The route builder writes the
canonical configured value as `Message.INBOUND_ROUTE_ACTION`, a Camel Exchange
property. A body value or client header cannot replace it. Different paths,
versions, or gateways may intentionally bind to the same service action.
Actions not exposed by one gateway remain valid for another gateway or a
trusted internal caller.

## Inbound-action rules

Configured names are trimmed and lower-cased with `Locale.ROOT`, then must
match:

```text
[a-z][a-z0-9-]*(\.[a-z][a-z0-9-]*)*
```

The maximum length is 100 characters. Unsupported characters are rejected;
they are never replaced. Therefore `otp.verify` and `otp-verify` are different,
while ` OTP.Verify ` canonicalizes to `otp.verify`. Duplicate detection is
case-insensitive after canonicalization.

The action is never derived from a request body, route index, hash, route ID,
or arbitrary client-controlled header.

## Startup planning and validation

During route construction, `ActionDispatchRoutePlanFactory` validates every
active binding and builds an immutable `ActionDispatchPlan`:

```text
canonical Definition.details.inboundAction
  -> ServiceOperation
  -> one-step RoutingPlan(ACTION_DISPATCH)
```

`ActionDispatchPlanCatalog` shares the compiled plan between gateway, service,
and operation route construction. Each step contains the already resolved
fixed Operation route URI and existing routing metadata. The Operation metadata
lookup must find an active Operation before startup can continue.

Startup fails with `SCM_SERVICE_ACTION_CONFIGURATION_INVALID` when:

- no active executable binding exists;
- an active ServiceOperation has no referenced Definition;
- its Definition has missing, malformed, blank, invalid, or over-length
  `inboundAction`;
- canonical actions are duplicated;
- two actions reference the same Operation;
- `operationName` is blank, missing, or inactive;
- a technical `ACTION_PLAN` binding is used as an action;
- an active inbound route omits `inboundAction` or names an unknown action; or
- a generated child plan is not exactly one executable `ACTION_DISPATCH` step.

All referenced action Operations are collected by the Operation layer. It does
not register only the first binding.

No dispatch selector is stored in `REF.TBL_SCM_SERVICE_OPERATION`. The existing
`DEFINITION_ID` relationship supplies the Definition-owned selector, and the
existing unique service/Operation constraint continues to enforce the
no-alias rule. Canonical, case-insensitive duplicate validation remains
mandatory at startup.

## Runtime flow and performance

Runtime selection reads only `Message.INBOUND_ROUTE_ACTION`, canonicalizes the
trusted configured value, and performs an O(1) immutable-map lookup. It does not
read the database, parse JSON, use reflection, construct routes or endpoints,
evaluate a dynamic expression, or use `toD()`.

The selected one-step plan executes through the same `RoutingStepExecutor` as
existing routing. That executor preserves the request headers, uses the
original request body as the action request, and sets
`Message.SERVICE_OPERATION` and `Message.OPERATION_NAME` before entering the
fixed Operation route. Provider failures are not retried by this strategy.

## Failure behavior

Runtime selection has two safe stable errors:

| Condition | Error code | Status |
| --- | --- | --- |
| Missing/blank Exchange action | `SCM_SERVICE_ACTION_REQUIRED` | `SC_ERROR_VALIDATION` |
| Unknown or invalid Exchange action | `SCM_SERVICE_ACTION_NOT_FOUND` | `SC_NOT_FOUND` |

Startup/build failures use
`SCM_SERVICE_ACTION_CONFIGURATION_INVALID`. The exact-type resolver feeds all
three through the standard `ScmException -> ScmFault -> caller formatter`
path. Runtime messages do not enumerate configured actions or reveal bean
names, route IDs, endpoints, or provider implementation details. Errors from
the selected Operation and Provider continue through their existing resolvers
unchanged, on the same Exchange and correlation context.

## Observability and sensitive data

The existing service and operation spans and metrics remain in place; dispatch
does not add a lookup-only span. The existing routing-step observation records
bounded service code, canonical service action, Operation name, routing
strategy, and outcome. Never add bodies, OTPs, usernames, credentials, tokens,
authorization headers, or other request values to traces, logs, audits, or
metric tags. Metrics continue through Actuator, Micrometer, Prometheus, and
Grafana and are not written to files.

## Complete OTP example with `scm:uaa`

The external and service configuration is:

```text
POST /otp/verify
  -> INBOUND Definition.details.inboundAction = otp.verify
  -> Service OTP, routingStrategy = ACTION_DISPATCH
  -> Definition.details.inboundAction = otp.verify
  -> ServiceOperation(operationName = UAA_OTP_VERIFY,
                      active = true)
```

The existing provider configuration is:

```text
OperationProvider:
  name   = UAA_RESOURCE
  uri    = scm:uaa
  active = true

Operation:
  name     = UAA_OTP_VERIFY
  type     = PROVIDER
  provider = UAA_RESOURCE
  path     = otp.verify
  active   = true
```

For this configuration, the `uaa` Resource bean belongs in the `scm-web`
application context and injects a typed UAA HTTP client. It must not inject
server-side beans from the separate `scm-uaa` process:

```java
public interface UaaResource {
    OtpVerifyResult verifyOtp(OtpVerifyCommand command);
}

@ScmResource("uaa")
public class DefaultUaaResource implements UaaResource {
    private final UaaOtpClient otpClient;

    public DefaultUaaResource(UaaOtpClient otpClient) {
        this.otpClient = otpClient;
    }

    @Override
    @ResourceAction("otp.verify")
    public OtpVerifyResult verifyOtp(OtpVerifyCommand command) {
        return otpClient.verify(command);
    }
}
```

The full runtime path is then:

```text
POST /otp/verify
  -> configured otp.verify Exchange property
  -> immutable OTP action map
  -> direct:op.UAA_OTP_VERIFY
  -> Operation(type=PROVIDER, path=otp.verify)
  -> scm:uaa
  -> @ResourceAction("otp.verify") on the Spring-managed uaa Resource
  -> typed UAA HTTP client
```

The Resource/provider contract is documented separately in
`scm-provider-scm/README.md`. This strategy only selects the Operation; it does
not expand or alter the `scm:` provider.

## Troubleshooting

- `SCM_SERVICE_ACTION_REQUIRED` at runtime: a trusted caller entered the service
  route without the configured Exchange property. Gateway routes must use the
  existing binder.
- `SCM_SERVICE_ACTION_NOT_FOUND`: the trusted property does not match the
  startup-compiled map. Check the canonical action, not an Operation or Java
  method name.
- Configuration-invalid for an inbound route: add a valid `inboundAction` to
  `Definition.details` and ensure that service has the same action binding.
- Configuration-invalid for a binding: check activity, syntax, duplicates,
  aliases, `ACTION_PLAN` use, and the referenced active Operation.
- A provider is inactive or unavailable at startup: activate/fix the provider
  and Operation through the existing provider lifecycle configuration. Do not
  bypass it with a dynamic endpoint.
- A configured service action is not exposed on one gateway: this is valid. Add
  an `INBOUND` route only when that gateway should expose it.

## Version 1 limitations

Aliases remain unsupported: there is no many-actions-to-one-Operation mapping.
Each request executes one action and one Operation. Workflow state, failover,
automatic retry, and provider base URL/path joining are separate designs and
are intentionally not part of `ACTION_DISPATCH`.
