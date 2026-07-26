# Defining TASK_WORKFLOW services

This is the canonical configuration and runtime guide for services whose
`Service.routingStrategy` is `TASK_WORKFLOW`.

## Architecture overview

`inboundAction` and `ActionPlan.name` are deliberately different identities:

```text
inboundAction
    -> what the client requested

ActionPlan.name
    -> which internal orchestration implements that request
```

For example:

```text
serviceCode:     PAYMASTER_MANAGEMENT
inboundAction:   approve_and_execute
ActionPlan.name: paymaster-approval-default
```

The current binding is deterministic:

```text
serviceCode + inboundAction
    -> exactly one active ActionPlan
```

The two values must not be treated as equal. Keeping them separate allows the
internal plan to change later without changing the external client contract.

The complete runtime path is:

```text
REST request
    -> shared TASK_WORKFLOW gateway category
    -> REST path-variable normalization
    -> TaskWorkflowRouteIdentityResolver
    -> canonical serviceCode and inboundAction
    -> gateway-owned serviceVersion
    -> ClientContract and RequestContractDecoder
    -> resolved service route
    -> immutable ActionPlan lookup
    -> RoutingStrategyEngine
    -> ordered ActionPlan steps
    -> direct:op.<operationName>
    -> operation provider
    -> GLOBAL_RESPONSE_HANDLER
    -> ResponseContractEncoder

exception
    -> GLOBAL_ERROR_HANDLER
    -> ScmFault
    -> FaultContractEncoder
```

TASK_WORKFLOW uses the existing generic gateway, service, operation, response,
and fault pipelines. It does not have a separate gateway route builder, Camel
component, response encoder, or fault encoder.

## Responsibility boundaries

| Component | Owns | Does not own |
| --- | --- | --- |
| Gateway | Protocol exposure, shared route category, gateway route version, client contract, decoder and encoders | Workflow steps, operation names, decision policies, retry cursor |
| `TaskWorkflowRouteIdentityResolver` | Protocol-specific extraction of `serviceCode` and `inboundAction` | Service version, action-plan parsing, business routing |
| Service layer | Resolving the selected service-owned plan and invoking its strategy | REST path parsing and response serialization |
| Action-plan registry | Immutable `serviceCode + inboundAction` binding | Gateway contract version |
| Routing strategy engine | Step ordering, continuation, and strategy-owned output selection | Provider persistence and protocol encoding |
| `RoutingDecisionPolicy` | Pure classification of one step result | Persistence, process mutation, events, encoders |
| Execution coordinator | Execution identity, snapshots, recovery, attempts, persistence, idempotent terminal results | Protocol syntax and provider implementation |
| Recovery store | Durable watcher JSON and process-level locking | Plan selection and routing decisions |
| Operation route | Existing provider resolution and invocation | Action-plan ownership |
| Response/fault encoders | Client-contract formatting | Workflow execution |

Normal services keep their statically resolved identity from
`RuntimeServicePlan` and their inbound route. Only a TASK_WORKFLOW category
passes through the dynamic task-workflow identity resolver.

## Gateway route and configuration boundary

### Shared REST inbound category

Configure one shared REST category equivalent to:

```text
type   = INBOUND
method = POST
path   = /task-workflow/{serviceCode}/{inboundAction}
```

At runtime, `method` and `path` are properties of
`InboundChannelServiceDefinition`. The current database entity has no `METHOD`
or `PATH` columns. `ChannelServiceDefinitionMapper` obtains those runtime
properties from the separate gateway definition linked by
`TBL_SCM_CHN_SVC_DEFINITION.DEFINITION_ID`.

A representative gateway `Definition.details` is:

```json
{
  "method": "POST",
  "path": "/task-workflow/{serviceCode}/{inboundAction}",
  "version": "v1"
}
```

This gateway JSON is a protocol/client-contract definition. It must not contain
service-owned action-plan fields.

Do not duplicate the same active method, path, and version for each service.
Route construction rejects duplicate shared categories. One category can
dynamically dispatch to all active TASK_WORKFLOW services present in the same
runtime route plan.

### Canonical route identity

For a request such as:

```text
POST /task-workflow/PAYMASTER_MANAGEMENT/approve_and_execute
```

the REST adapter extracts the two path variables into normalized inbound
parameters. `RestTaskWorkflowRouteIdentityResolver` then sets the canonical
values used by later layers:

```text
Message.SERVICE_CODE         = PAYMASTER_MANAGEMENT
Message.INBOUND_ROUTE_ACTION = approve_and_execute
Message.SERVICE_VERSION      = v1
```

`serviceCode` and `inboundAction` come from the path. `serviceVersion` comes
only from the versioned gateway route through `ClientContractVersionResolver`.
It is not stored in, parsed from, or used to select an ActionPlan.

### Non-REST protocols

The resolver registry is the extension point for ISO 8583, SOAP, messaging,
Kafka, TCP, and other protocols. Only the REST identity resolver is implemented
in the current production source. A new protocol requires its own
`TaskWorkflowRouteIdentityResolver`; without one, route identity resolution
fails safely.

Protocol adapters must also normalize workflow identifiers into either:

```text
canonical request payload
or
Message.INBOUND_PARAMETERS
```

The service and routing engines must not know whether a value originally came
from a REST path, ISO field, message property, Kafka header, SOAP element, or
TCP frame.

## ActionPlan definition

An ActionPlan is stored in the existing relationship:

```text
Service
    -> ServiceOperation
        -> Definition
            -> details
```

The owning definition must be classified in the runtime model as:

```text
Definition.type = ACTION_PLAN
```

A complete definition is:

```json
{
  "inboundAction": "approve_and_execute",
  "actionPlan": {
    "name": "paymaster-approval-default",
    "routingStrategy": "CHAIN_ON_APPROVE",
    "steps": [
      {
        "stepId": "approve-process",
        "stepType": "APPROVE_PROCESS",
        "operationName": "approve-paymaster-process"
      },
      {
        "stepId": "register-in-nab",
        "stepType": "BUSINESS_OPERATION",
        "operationName": "register-paymaster-in-nab",
        "decisionPolicy": "NAB_PAYMASTER_REGISTRATION"
      },
      {
        "stepId": "complete-process",
        "stepType": "COMPLETE_PROCESS",
        "operationName": "complete-paymaster-process"
      }
    ]
  }
}
```

The parser is strict. Unknown root, action-plan, or step fields are rejected.
Gateway version and response-selection settings do not belong in this JSON.

### Field reference

| Field | Required | Consumer | Meaning | Does not control |
| --- | --- | --- | --- | --- |
| `inboundAction` | Yes | Command resolver and action-plan registry | External intent bound to this plan | Plan name, gateway version, HTTP method/path |
| `actionPlan` | Yes | Action-plan parser | Container for internal orchestration | Gateway routing |
| `actionPlan.name` | Yes | Registry entry, snapshot, fingerprint, execution metadata | Internal orchestration identity | External action |
| `routingStrategy` | Yes | Engine registry | `FIRST` or `CHAIN_ON_APPROVE` | Service-level `TASK_WORKFLOW` selection |
| `steps` | Yes | Plan factory and strategy | Non-empty ordered step array | Gateway route order |
| `stepId` | Yes | Execution context, result map, snapshot, recovery, observation | Stable identity of this step occurrence | Executable operation lookup |
| `stepType` | Yes | Request mapper, capability validation, observation | Semantic workflow meaning | Operation selection or authorization role |
| `operationName` | Yes | Operation route resolver | Active executable `ServiceOperation` to call | Step semantics |
| `decisionPolicy` | No | Decision-policy registry | Pure result classifier; defaults to `DEFAULT_SUCCESS` | Persistence, side effects, route selection |

`stepId` values are case-insensitively unique within one plan. The same
executable operation may appear more than once if each occurrence has a
different `stepId`. `stepIndex` is assigned from array order and is not a JSON
field.

## Owning service operation

The ActionPlan must be linked from an active existing `ServiceOperation`.
Because the unchanged persistence model requires `operationName`, the owner
still needs a unique technical name, for example:

```text
service:          PAYMASTER_MANAGEMENT
active:           true
operationName:    task-workflow-paymaster-approval-plan
definition.type:  ACTION_PLAN
definition:       paymaster-approval-default-definition
```

That technical operation name is only the identity of the owning record. It is
not the inbound action, ActionPlan name, provider operation, or action-plan
lookup key.

When the linked definition type is `ACTION_PLAN`, route construction:

```text
parses and validates the plan
registers serviceCode + inboundAction
skips provider metadata resolution for the owner
does not create direct:op.task-workflow-paymaster-approval-plan
```

All other active service operations retain the existing executable route
behavior.

## Executable operations

Every `steps[].operationName` must identify:

1. an active `ServiceOperation`;
2. belonging to the same service;
3. not classified as an ActionPlan;
4. backed by active operation metadata and a valid provider route.

An executable step invokes:

```text
ActionPlan step
    -> direct:op.<operationName>
    -> Operation
    -> OperationProvider.uri
```

An ActionPlan owner cannot reference itself or another ActionPlan owner as a
step. A `BUSINESS_OPERATION` must target a business provider. All other
task/process step types must target an `scm-task:` provider supported by
`scm-provider-task`.

Example executable operations for the complete chain:

| Service operation | Provider category | Step type |
| --- | --- | --- |
| `approve-paymaster-process` | `scm-task:internal` | `APPROVE_PROCESS` |
| `register-paymaster-in-nab` | Business provider | `BUSINESS_OPERATION` |
| `complete-paymaster-process` | `scm-task:internal` | `COMPLETE_PROCESS` |

## Supported inbound actions

The current production command resolver accepts:

| Inbound action | Required strategy | Required structure |
| --- | --- | --- |
| `start` | `FIRST` | one `START_PROCESS` step |
| `task_complete` | `FIRST` | one `COMPLETE_TASK` step |
| `complete_task` | `FIRST` | alias for `task_complete` |
| `approve_and_execute` | `CHAIN_ON_APPROVE` | approve, business step(s), complete |
| `cancel_process` | `FIRST` | one `CANCEL_PROCESS` step |
| `find_processes` | `FIRST` | one `FIND_ALL_PROCESS` step |
| `find_tasks` | `FIRST` | one `FIND_ALL_TASK` step |
| `find_tasks_by_process_id` | `FIRST` | one `FIND_TASK_BY_PROCESS_ID` step |
| `update_process_description` | `FIRST` | one `UPDATE_PROCESS_DESCRIPTION` step |

No other aliases are resolved.

## Supported step types

`TaskWorkflowStepType` is shared by `scm-core` and `scm-provider-task`.

| Step type | Purpose and identifiers | `scm-task` support | Direct action / chain position | Typical operation |
| --- | --- | --- | --- | --- |
| `START_PROCESS` | Create a process; requires canonical `executionId`, no process ID yet | Yes | `start`, only `FIRST` step | `start-paymaster-process` |
| `COMPLETE_TASK` | Complete a task; requires `taskId` | Yes | task-complete action, only `FIRST` step | `complete-paymaster-approval-task` |
| `APPROVE_PROCESS` | Approve a process; requires `processId` | Yes | First step of `approve_and_execute` | `approve-paymaster-process` |
| `BUSINESS_OPERATION` | Execute business work using current process context | No | One or more middle chain steps | `register-paymaster-in-nab` |
| `COMPLETE_PROCESS` | Complete the current workflow process | Yes | Final chain step; not directly exposed | `complete-paymaster-process` |
| `CANCEL_PROCESS` | Cancel a process; requires `processId` | Yes | `cancel_process`, only `FIRST` step | `cancel-paymaster-process` |
| `FIND_ALL_PROCESS` | Query processes; no workflow ID required | Yes | `find_processes`, only `FIRST` step | `find-paymaster-processes` |
| `FIND_ALL_TASK` | Query tasks; no workflow ID required | Yes | `find_tasks`, only `FIRST` step | `find-paymaster-tasks` |
| `FIND_TASK_BY_PROCESS_ID` | Query tasks for `processId` | Yes | process-task query, only `FIRST` step | `find-paymaster-tasks-by-process` |
| `UPDATE_PROCESS_DESCRIPTION` | Update process description; requires `processId` | Yes | update action, only `FIRST` step | `update-paymaster-process-description` |

`BUSINESS_OPERATION` is an orchestration type. It is never executed by
`scm-provider-task`.

## Identifier normalization

For a REST request such as:

```text
POST /task-workflow/PAYMASTER_MANAGEMENT/task_complete
```

the shared route provides the action identity. A task identifier may be
supplied in the decoded payload or normalized inbound parameters. The service
layer reads normalized values only; it does not parse URL syntax.

Identifier requirements are:

| Step type | Required identifier |
| --- | --- |
| `COMPLETE_TASK` | `taskId` |
| `APPROVE_PROCESS` | `processId` |
| `CANCEL_PROCESS` | `processId` |
| `FIND_TASK_BY_PROCESS_ID` | `processId` |
| `UPDATE_PROCESS_DESCRIPTION` | `processId` |
| `COMPLETE_PROCESS` | current workflow `processId` |

`processId` may be represented by `processId` or `id` in the normalized
payload. Inbound parameters use the canonical identifier name.

The input resolver fails fast for missing, blank, nonnumeric, out-of-range, or
fractional values. If payload and inbound parameters both supply the same
identifier, they must be equal. Conflicting aliases or sources fail instead of
choosing one silently.

## Routing strategies and response selection

### FIRST

`FIRST` requires exactly one step. That step is classified by its configured
decision policy. On success, its canonical output is the strategy output. On a
retryable outcome, its current output is returned only after durable state is
saved. A failure is thrown.

There is no shortcut that treats an arbitrary non-`Message` value as success;
the generic classifier or configured policy classifies every result.

### CHAIN_ON_APPROVE

The valid `approve_and_execute` shape is:

```text
APPROVE_PROCESS
    -> one or more BUSINESS_OPERATION
    -> COMPLETE_PROCESS
```

Steps run in JSON array order. A successful step with a following step causes
internal continuation. A retryable result stops the chain and returns that
step's output after persistence. A failed result throws and later steps do not
run.

On final success, the strategy returns the last successful
`BUSINESS_OPERATION` result. If a valid future chain contains no business step,
the deterministic fallback is the last successful step result. Response
selection belongs to the strategy and is not a definition field.

Earlier successful results are stored by `stepId` in `stepResults` and are
available to later business steps.

## Routing decisions

The public routing decision contract contains:

| Decision | Runtime behavior |
| --- | --- |
| `SUCCESS` | Record success; run the next step when present, otherwise complete; use normal response encoding |
| `RETRY_LATER` | Save a durable retry snapshot first; then return a normal canonical retryable response |
| `FAIL` | Persist failure metadata when possible; throw a typed routing exception; use global fault encoding |

`RETRY_LATER` is not an exception. If durable retry persistence fails, the
coordinator throws a typed persistence exception and no retryable response is
returned.

A decision policy only classifies an outcome. It must not update process/task
state, write snapshots, publish lifecycle events, or invoke gateway encoders.

## Execution identity, process correlation, and compatibility

`Message.EXECUTION_ID` is the canonical TASK_WORKFLOW execution identity.
`JsonScmRequestDecoder` can normalize it from the JSON payload or
`X-SCM-Execution-ID`. The task-workflow resolver also accepts normalized
inbound parameters and checks all supplied values for conflicts.

The `start` action requires a client-supplied execution identity. When no
explicit execution ID is present, the existing client-correlation header is a
fallback. The value is stored in the unchanged
`ProcessInstance.correlationId` field when the process is created.

Approve behavior is backward compatible:

```text
process already has correlationId
    -> preserve it
    -> do not overwrite it from the approve request

process correlationId is null or blank
    -> require approve request correlationId
    -> store it through the existing field
```

The first path preserves the stable workflow execution correlation established
during start. The second path preserves legacy/non-workflow behavior for
processes created without a correlation. An approve request correlation is not
automatically a workflow execution ID.

There is no database uniqueness constraint on process correlation. The current
start path performs a lookup before insert and can return an existing process,
but this is not a cluster-wide exactly-once guarantee for concurrent starts.

## Durable snapshots and recovery

TASK_WORKFLOW requires exactly one `TaskWorkflowRecoveryStore`. The
`scm-provider-task` implementation uses the unchanged watcher entity:

```text
TBL_PRC_PROCESS_INSTANCE_WATCHER
    PROCESS_ID = workflow process ID
    TYPE       = WORKFLOW_EXECUTION (3)
    ROW_NO     = 0
    DATA       = versioned snapshot JSON
```

The snapshot records execution identity, external action, internal ActionPlan
name, gateway contract version, definition ID, deterministic fingerprint,
strategy, state, decision, process ID, stable step identities, minimal recovery
context, attempts, and timestamps. It excludes Camel exchanges, Java exception
objects, stack traces, authentication data, sensitive headers, and unfiltered
provider payloads.

The plan fingerprint is SHA-256 over an explicitly ordered representation of:

```text
serviceCode
inboundAction
ActionPlan.name
definitionId
routingStrategy
ordered stepIndex, stepId, stepType, operationName, decisionPolicy
```

Gateway service version and JSON formatting are excluded from the fingerprint.
The version is retained separately in the snapshot to prevent a retry from
returning through a different client contract.

Recovery is allowed only when the persisted top-level decision is
`RETRY_LATER`:

```text
no explicit executionId
    -> new execution

explicit executionId
    -> locate process and load snapshot

snapshot SUCCESS
    -> return stored successful response idempotently

snapshot RETRY_LATER
    -> validate execution, gateway version, plan identity, steps, and cursor
    -> resume according to strategy

snapshot FAIL
    -> recreate and throw stored typed failure

snapshot missing, corrupt, or plan changed
    -> fail safely; never restart blindly
```

For `FIRST`, a retry starts at step zero. For `CHAIN_ON_APPROVE`, every earlier
step must be persisted as `SUCCESS`, and the selected step must be the first
`RETRY_LATER` step with matching `stepId` and `stepIndex`.

Before a retry, the provider takes a short pessimistic process lock, validates
the snapshot, writes a new attempt marker, and commits. The remote provider is
then called without the database lock. A second short transaction accepts the
outcome only when the attempt marker still matches. The provider idempotency
key is derived from `executionId + stepId` and exposed as task-workflow
exchange metadata. A provider adapter must forward or consume that key for it
to be effective. Exactly-once execution still depends on provider idempotency
or inquiry support.

If a pod crashes after a remote call but before the outcome is saved, the
active attempt is not blindly replayed. Operational inquiry or provider
idempotency is required to resolve the unknown outcome.

## Persistence relationships

The verified configuration relationships are:

```text
Service (REF.EB_SERVICE)
  -> ChannelServiceAccess (REF.CHANNEL_SERVICE_ACCESS)
      -> ChannelServiceDefinition (REF.TBL_SCM_CHN_SVC_DEFINITION)
          -> GatewayChannel (REF.TBL_SCM_GATEWAY_CHANNEL)
          -> gateway Definition (REF.TBL_SCM_DEFINITION)

Service (REF.EB_SERVICE)
  -> ServiceOperation (REF.TBL_SCM_SERVICE_OPERATION)
      -> ActionPlan Definition (REF.TBL_SCM_DEFINITION)

ServiceOperation.operationName
  -> active Operation.name (REF.TBL_SCM_OPERATION)
      -> OperationProvider (REF.TBL_SCM_OPERATION_PROVIDER)
```

| Record | TASK_WORKFLOW responsibility | Important relationship | Scope |
| --- | --- | --- | --- |
| `EB_SERVICE` | Service code, publish state, `TASK_WORKFLOW` routing strategy | `EB_SERVICE_ID` | One service |
| `CHANNEL_SERVICE_ACCESS` | Active channel-to-service permission | channel and `EB_SERVICE_ID` | One service/channel binding |
| `TBL_SCM_CHN_SVC_DEFINITION` | Shared inbound category membership and gateway | access, gateway, type, definition | Shared external category; do not duplicate per action |
| `TBL_SCM_GATEWAY_CHANNEL` | Active protocol endpoint | channel and protocol type | Shared |
| `TBL_SCM_DEFINITION` | Gateway details or ActionPlan JSON, depending on the owning relationship | `DEFINITION_ID` | Separate record per role/action plan |
| `TBL_SCM_SERVICE_OPERATION` | Active technical ActionPlan owner or executable operation name | service and definition | One ActionPlan owner per external action; executable records may be reused by steps |
| `TBL_SCM_OPERATION` | Active executable operation metadata | name and provider | Shared operation catalog |
| `TBL_SCM_OPERATION_PROVIDER` | Active provider URI | provider ID | Shared provider |

There are no task-workflow columns on `ServiceOperationEntity`. In particular,
external action and gateway version are not top-level service-operation
properties. Action-plan binding is parsed from the linked definition JSON.

There are also no route `METHOD` or `PATH` columns on
`TBL_SCM_CHN_SVC_DEFINITION`; the mapper reads them from the linked gateway
definition as described above.

## Startup and route-construction validation

Startup or route construction rejects:

- a TASK_WORKFLOW service with no active ActionPlan;
- a missing or non-`ACTION_PLAN` definition;
- blank service code, source service-operation ID, technical operation name, or definition ID;
- blank or invalid JSON details;
- unsupported JSON fields;
- missing or unsupported `inboundAction`;
- missing or blank ActionPlan name;
- unsupported strategy;
- empty steps;
- blank or duplicate `stepId`;
- invalid `stepType`;
- blank `operationName`;
- blank or unregistered decision-policy code;
- duplicate active `serviceCode + inboundAction` binding;
- invalid `FIRST` step count or direct-action step type;
- invalid `approve_and_execute` order;
- missing, inactive, or cross-service executable operation;
- an ActionPlan owner referenced as an executable step;
- a business step targeting `scm-task`;
- a non-business step targeting a non-task provider;
- a task-provider capability mismatch;
- a missing or duplicate recovery-store implementation;
- a missing shared REST category;
- a shared category without both route variables;
- duplicate shared method/path/version;
- gateway category details that contain service-owned plan fields.

## Complete examples

### Minimal FIRST plan: start

Shared request:

```text
POST /task-workflow/PAYMASTER_MANAGEMENT/start
X-SCM-Execution-ID: paymaster-start-20260726-0001
```

ActionPlan definition:

```json
{
  "inboundAction": "start",
  "actionPlan": {
    "name": "paymaster-start-default",
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepId": "start-process",
        "stepType": "START_PROCESS",
        "operationName": "start-paymaster-process"
      }
    ]
  }
}
```

Required executable service operation:

```text
start-paymaster-process -> active Operation -> scm-task:internal
```

Resolved identity:

```text
serviceCode     = PAYMASTER_MANAGEMENT
inboundAction   = start
ActionPlan.name = paymaster-start-default
serviceVersion  = gateway route version
executionId     = paymaster-start-20260726-0001
```

On success, the process is created, its correlation is initialized from the
execution ID, the snapshot is completed after a process ID is available, and
the start-step output is returned through the normal response encoder.

### Complete task

Request:

```text
POST /task-workflow/PAYMASTER_MANAGEMENT/task_complete
```

Payload:

```json
{
  "taskId": 48125,
  "decision": "APPROVE",
  "description": "Approved"
}
```

Definition:

```json
{
  "inboundAction": "task_complete",
  "actionPlan": {
    "name": "paymaster-complete-approval-task",
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepId": "complete-approval-task",
        "stepType": "COMPLETE_TASK",
        "operationName": "complete-paymaster-approval-task"
      }
    ]
  }
}
```

The normalized operation request includes `taskId`. If the same ID is also
provided in inbound parameters, it must match.

### Full CHAIN_ON_APPROVE plan

Request:

```text
POST /task-workflow/PAYMASTER_MANAGEMENT/approve_and_execute
X-SCM-Execution-ID: paymaster-start-20260726-0001
```

Payload:

```json
{
  "processId": 91004,
  "correlationId": "legacy-request-correlation-if-needed"
}
```

Definition:

```json
{
  "inboundAction": "approve_and_execute",
  "actionPlan": {
    "name": "paymaster-approval-default",
    "routingStrategy": "CHAIN_ON_APPROVE",
    "steps": [
      {
        "stepId": "approve-process",
        "stepType": "APPROVE_PROCESS",
        "operationName": "approve-paymaster-process"
      },
      {
        "stepId": "register-in-nab",
        "stepType": "BUSINESS_OPERATION",
        "operationName": "register-paymaster-in-nab",
        "decisionPolicy": "NAB_PAYMASTER_REGISTRATION"
      },
      {
        "stepId": "grant-scm-access",
        "stepType": "BUSINESS_OPERATION",
        "operationName": "grant-paymaster-account-access-in-scm"
      },
      {
        "stepId": "complete-process",
        "stepType": "COMPLETE_PROCESS",
        "operationName": "complete-paymaster-process"
      }
    ]
  }
}
```

Required executable service operations:

```text
approve-paymaster-process
register-paymaster-in-nab
grant-paymaster-account-access-in-scm
complete-paymaster-process
```

Success flow:

```text
approve SUCCESS
    -> NAB registration SUCCESS
    -> SCM access SUCCESS
    -> complete process SUCCESS
    -> return SCM access result
```

Retryable flow:

```text
approve SUCCESS
    -> NAB registration RETRY_LATER
    -> persist RETRY_PENDING snapshot
    -> skip later steps
    -> return NAB step result as a normal retryable response
```

On a later request with the same execution ID, recovery validates the same
gateway version and ActionPlan fingerprint, verifies the successful approve
step, and resumes at `register-in-nab`.

## Developer setup checklist

1. Create or identify an active published service.
2. Set its routing strategy to `TASK_WORKFLOW`.
3. Create active provider-backed executable operations.
4. Connect each executable operation to the same service as an active `ServiceOperation`.
5. Verify the executable operations produce `direct:op.<operationName>` routes.
6. Create a separate definition containing the ActionPlan JSON.
7. Set the definition's runtime type to `ACTION_PLAN`.
8. Create the active, non-executable owning service operation with a unique technical name.
9. Bind external `inboundAction` to a distinct internal `ActionPlan.name`.
10. Configure unique step IDs, semantic step types, executable operation names, and policies.
11. Configure one active channel access and active gateway channel.
12. Configure one shared inbound category with the two route variables.
13. Confirm the gateway category and ActionPlan use separate definitions.
14. Confirm exactly one durable task-workflow recovery store is active.
15. Start the application and resolve every route-construction validation error.
16. Verify safe observation fields for service, action, strategy, execution, step, and decision.
17. Verify provider idempotency/inquiry behavior before enabling retries.

## Troubleshooting

| Symptom | Likely cause | Inspect and correct |
| --- | --- | --- |
| Shared route is not created | Missing/inactive gateway, access, or inbound category | Gateway channel, channel access, category type/method/path/version |
| Route variables unavailable | Path omits `{serviceCode}` or `{inboundAction}` | Gateway definition details and normalized inbound parameters |
| Action plan not found | No active runtime `ACTION_PLAN` binding for service/action | Owning service operation, linked definition type/details |
| Duplicate action plan | Two active plans bind the same service/action | Deactivate or rebind one owner |
| ActionPlan owner gets an operation route | Runtime definition was not classified as `ACTION_PLAN` | Definition type supplied to the runtime model |
| Executable operation not found | Name is absent, inactive, cross-service, or not in operation catalog | Service operation and active Operation metadata |
| Invalid policy | Blank or unknown policy code | `decisionPolicy` and registered `RoutingDecisionPolicy.code()` |
| Invalid chain order | Missing approve/business/complete structure | JSON array order and step types |
| Identifier unavailable | Adapter did not normalize `taskId`/`processId` | Canonical payload and `Message.INBOUND_PARAMETERS` |
| Conflicting identifier | Payload and inbound parameters disagree | Send one value or equal values |
| Retry rejected | Snapshot is not `RETRY_LATER`, execution ID differs, or attempt is active | Watcher state and safe execution metadata |
| Plan changed during retry | Definition ID or fingerprint differs | Finish/reconcile old execution; do not replay against new plan |
| Snapshot missing or corrupt | Watcher absent/invalid or deployed DATA capacity exceeded | Recovery store and watcher row; do not restart blindly |
| Recovery store missing | Provider module disabled or multiple stores registered | Task-provider auto-configuration |
| Concurrent attempt rejected | Another pod owns the active attempt marker | Wait for reconciliation; do not force replay |
| Provider outcome unknown after crash | Remote call completed without saved result | Provider inquiry/idempotency; never blind replay |
| Legacy approve rejects correlation | Process has no stored correlation and request omitted it | Supply legacy approve `correlationId` |
| Workflow correlation appears unchanged after approve | Expected behavior | Existing process correlation is stable and is never overwritten |

## Current limitations and extension points

- `Definition.details` retains the existing 2048-character validation limit.
  Keep realistic plans below that limit; no CLOB or schema expansion is part of
  this architecture.
- `DefinitionEntity.type` is currently a transient JPA property. The database
  row therefore does not durably retain `ACTION_PLAN` after a reload. The
  runtime configuration source must supply that enum value; otherwise route
  construction reports no active ActionPlan. This cannot be fixed under the
  no-entity/no-database-change constraint.
- The watcher `DATA` mapping is unchanged and declares no expanded capacity.
  The deployed database limit is authoritative. An oversized snapshot fails as
  a typed persistence error rather than returning a retryable response.
- Action-plan uniqueness is enforced in memory during route construction, not
  by a database constraint.
- Start idempotency has no database uniqueness constraint and is not a hard
  cluster-wide guarantee.
- Remote exactly-once behavior depends on provider idempotency or inquiry.
- Only REST has a task-workflow route identity resolver today.
- Future contextual selection may choose different ActionPlans by channel,
  tenant, segment, feature flag, policy, effective date, or service profile.
  Those selectors are not implemented.
- A future strategy may add forward and compensation cursors. The current
  recovery-policy registry already keeps cursor selection strategy-specific.
- Gateway client-contract versioning and ActionPlan evolution remain separate.
