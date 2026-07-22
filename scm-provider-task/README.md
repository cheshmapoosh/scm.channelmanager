# scm-provider-task

## Purpose

`scm-provider-task` exposes the task/process workflow engine through the
`scm-task` Camel component. It is a provider module, not a gateway or service
router.

```text
Camel component scheme: scm-task
Canonical provider URI: scm-task:internal
providerCode:           internal
engine-type:            internal
```

The provider boundary is intentionally small:

```text
scm-task:<providerCode>
Message.TASK_WORKFLOW_ROLE = <TaskWorkflowRole>
```

`providerCode` selects the configured task engine. `TaskWorkflowRole` selects
the semantic workflow action.

## Provider Configuration

The host enables the provider with:

```yaml
scm:
  provider:
    task:
      enabled: true
      providers:
        internal:
          enabled: true
          engine-type: internal
```

If no providers map is configured, `internal -> internal` is used as the default
provider mapping. Unknown provider codes fail fast when the endpoint is created
or used. Unknown enabled engine types fail during provider validation.

## Endpoint Contract

The endpoint shape is fixed:

```text
scm-task:<providerCode>
```

For the internal provider:

```text
OperationProvider.name = TASK_INTERNAL
OperationProvider.uri  = scm-task:internal
```

The endpoint segment after `scm-task:` is always a provider code. It is not an
operation name, service code, command, or routing strategy.

## TaskWorkflowRole

The provider resolves the role from exchange context:

```text
1. Message.TASK_WORKFLOW_ROLE
2. scmTaskWorkflowRole
```

Supported roles:

```text
START_PROCESS
APPROVE_PROCESS
COMPLETE_PROCESS
CANCEL_PROCESS
COMPLETE_TASK
FIND_ALL_TASK
FIND_ALL_PROCESS
FIND_TASK_BY_PROCESS_ID
UPDATE_PROCESS_DESCRIPTION
```

If the role is missing, the provider fails fast:

```text
scm-task:<providerCode> requires TaskWorkflowRole from exchange context.
```

The provider does not resolve roles from service records, operation records, or
legacy cartable operation-code names. Any compatibility or migration from old
configuration belongs before the request reaches `scm-provider-task`.

## Runtime Boundary

Runtime flow:

```text
Gateway
  -> matches the inbound route and supplies inboundAction
Service layer
  -> resolves inboundAction and its ordered routing plan
  -> selects the shared FIRST or CHAIN_ON_APPROVE engine
  -> executes configured operations by operationName
Operation layer
  -> routes to Operation.provider.uri
scm-provider-task
  -> validates providerCode
  -> executes TaskWorkflowRole on the configured engine
```

`scm-provider-task` does not decide which business service should use task
workflow. It does not know service routing strategies, service codes, or gateway
paths.

## Persistence

Spring Boot loads:

```text
ir.daneshrefah.scm.provider.task.autoconfigure.ScmTaskProviderAutoConfiguration
```

Default mode uses the host primary persistence beans:

```text
entityManagerFactory
transactionManager
```

Dedicated mode uses configured bean names:

```yaml
scm:
  provider:
    task:
      enabled: true
      entity-manager-factory: taskProviderEntityManagerFactory
      transaction-manager: taskProviderTransactionManager
```

If neither bean-name property is configured, default mode is used. If either one
is configured, both are required and the named beans must exist at startup.

## Events and Observation

Task/process lifecycle events are published through the existing
`scm-common` event provider contract:

```text
ir.daneshrefah.scm.common.event.provider
```

Safe process/task identifiers and status metadata may be published. Sensitive
request data must not be logged, traced, audited, or emitted as metric tags.

## Defining a TASK_WORKFLOW Service

The responsibilities are separated as follows:

```text
gateway route -> TASK_WORKFLOW command selection -> FIRST / CHAIN_ON_APPROVE
                                                   |
                                                   v
                                            direct:op.* routes
                                                   |
                       +---------------------------+------------------+
                       |                                              |
              scm-provider-task                              business providers
              task/process behavior                          NAB and SCM operations
```

`EB_SERVICE.ROUTING_STRATEGY=TASK_WORKFLOW` means that an inbound action selects
a command plan. It is not an operation engine. The inbound definition selects
the reusable engine through `taskWorkflow.routingStrategy`: `FIRST` executes one
operation; `CHAIN_ON_APPROVE` executes ordered operations until a policy says to
retry later or fail. Ordinary services use these same engines.

### Definition checklist

1. Create or select the task provider and required business providers.
2. Create each `Operation`, connect it to an `OperationProvider`, and connect it
   to the `Service` through an active `ServiceOperation`.
3. Set the service routing strategy to `TASK_WORKFLOW`.
4. Create one `InboundChannelServiceDefinition` for each external action.
5. Put the command JSON in that inbound row's `Definition.details`.
6. Start the application and correct every plan validation error.
7. Test each action, including retry and duplicate calls.

The configuration uses these existing model properties; no additional SQL
column names are implied by this guide:

| Model | Properties used by routing |
| --- | --- |
| `Service` | `code`, `routingStrategy`, `serviceOperations` |
| `ServiceOperation` | `active`, `operationName`, `definition` |
| `Operation` | `name`, `active`, `provider` |
| `OperationProvider` | `name`, `uri`, `active` |
| `InboundChannelServiceDefinition` | `method`, `path`, `definition` |
| `Definition` | `name`, `details`, `type` |

This module has no stable service seed or migration example: its `ddl.sql` is
empty. Define the relationships through the repository's normal asset/config
management path and place only the JSON contract in `Definition.details`.

The exact command shape is:

```json
{
  "inboundAction": "task_complete",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "role": "COMPLETE_TASK",
        "operationName": "complete-paymaster-approval-task"
      }
    ]
  }
}
```

Array position is execution order. Every step requires `role` and
`operationName`. For `CHAIN_ON_APPROVE`, `decisionPolicy` is optional and
defaults to `DEFAULT_SUCCESS`; `FIRST` does not require or evaluate a chain
policy. Do not add `executionOrder`. `operationName`, not `role`, selects the
connected operation. Several differently named
`BUSINESS_OPERATION` steps are valid; duplicate operation names in one action
are rejected.

`FIRST` requires exactly one step. The canonical `task_complete` action must be
`FIRST` with one `COMPLETE_TASK` step. `COMPLETE_PROCESS` cannot be a direct
action. The canonical `approve_and_execute` action must use
`CHAIN_ON_APPROVE`; its first role is `APPROVE_PROCESS`, its last role is
`COMPLETE_PROCESS`, and every middle role is `BUSINESS_OPERATION`.

### My Paymaster

A company introduces one user as paymaster. Every required signatory can
approve or reject. The seal user may also be a signatory. In this phase the seal
user's approval task and send task stay separate because they have different
purposes.

The `start` action uses `FIRST` and `start-paymaster-process`:

```json
{
  "inboundAction": "start",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "role": "START_PROCESS",
        "operationName": "start-paymaster-process"
      }
    ]
  }
}
```

For My Paymaster, that business operation must create one approval task per
required signatory and a separate send task for the seal user, even when the
seal user is also a signatory. The generic task provider represents signer
participants with `ProcessInstanceStartRequest.users` and its confirmation user
with `confirmUser`. In the current provider implementation, a separate global
confirmation task is created at start only when `confirmUser` is not already in
`users`; otherwise it is created after signer completion. A My Paymaster
adapter/provider extension is therefore required to guarantee two separate
tasks from process start. The routing engine does not merge or create tasks.

An approval is handled by `task_complete` and
`complete-paymaster-approval-task`:

```json
{"taskId":85410,"decision":"APPROVE"}
```

```json
{
  "taskId": 85410,
  "decision": "REJECT",
  "description": "The introduced paymaster is not approved."
}
```

The external `decision` is a service contract. The current task-provider model
uses `TaskRequest.action=COMPLETE` for approval and `TaskRequest.action=CANCEL`
for rejection, so the operation transformer must perform that mapping. Current
provider `CANCEL` behavior cancels the process and its remaining pending tasks;
that is existing behavior, not a new routing-engine rule. Ownership and
actionable-state checks remain inside `TaskManagementServiceImpl`. Duplicate
completion is not currently idempotent and must be addressed in the owning
business/provider contract before relying on duplicate delivery.

The send action is one ordered plan:

```json
{
  "inboundAction": "approve_and_execute",
  "taskWorkflow": {
    "routingStrategy": "CHAIN_ON_APPROVE",
    "steps": [
      {
        "role": "APPROVE_PROCESS",
        "operationName": "approve-paymaster-process-for-execution"
      },
      {
        "role": "BUSINESS_OPERATION",
        "operationName": "register-paymaster-in-nab",
        "decisionPolicy": "NAB_PAYMASTER_REGISTRATION"
      },
      {
        "role": "BUSINESS_OPERATION",
        "operationName": "grant-paymaster-account-access-in-scm"
      },
      {
        "role": "COMPLETE_PROCESS",
        "operationName": "complete-paymaster-process"
      }
    ]
  }
}
```

The approve-process operation must validate and lock the process/send task using
a database-backed concurrency mechanism. It checks ownership, verifies that
every required approval is approved and none rejected, and only then claims the
send work and returns stable transaction data. An early send returns the SCM
domain error, such as `REQUIRED_APPROVALS_NOT_COMPLETED`, without changing the
process or send task and without calling NAB or creating an SCM permission.
The current generic task repositories use ordinary `JpaRepository` lookups and
do not provide a pessimistic lock or `@Version`; a My Paymaster implementation
must add that protection in its owning domain rather than relying on a JVM lock.

Each business request receives this stable envelope:

```json
{
  "processId": 98541,
  "correlationId": "correlation-value",
  "transactionData": {},
  "stepResults": {}
}
```

Later steps access earlier responses through `stepResults[operationName]`. This
lets the permission operation use approved transaction data and the NAB result
without putting paymaster rules in the engines. After a successful send chain,
the service response remains the last `BUSINESS_OPERATION` response; the
internal `COMPLETE_PROCESS` response is not exposed as the business result.

| NAB result | Decision | What happens next |
| --- | --- | --- |
| success | `CONTINUE` | grant permission, then complete |
| duplicate registration | `CONTINUE`, normalized as `SUCCESS_ALREADY_APPLIED` | grant permission, then complete |
| timeout, no response, reset, EOF, unknown outcome | `RETRY_LATER` | stop; do not grant or complete |
| definitive business failure | `FAIL` | stop and use the existing SCM error flow |

The `NAB_PAYMASTER_REGISTRATION` policy recognizes a response already normalized
to `SUCCESS_ALREADY_APPLIED`. The repository contains no verified
paymaster-registration duplicate code, so the NAB operation/transformer must
normalize its configured duplicate response before the policy runs. Do not use
the generic NAB duplicate code as a paymaster code without a confirmed NAB
contract. NAB registration, permission grant, and completion must be
idempotent. An existing permission is success rather than another row. After an
unknown NAB response, a retry may receive duplicate registration, normalize it
to `SUCCESS_ALREADY_APPLIED`, and continue. The current checkout also has no
company/paymaster/account/permission unique-key repository; that upsert belongs
to the SCM business operation, not `scm-provider-task` or the routing engine.

Every executed step uses its existing `direct:op.*` route and receives its own
operation span under the service span. Skipped steps create no span. Safe span
attributes identify service, operation, strategy, step index, inbound action,
role, decision, and normalized outcome; payloads and unrestricted transaction
data are excluded.

Common startup errors are a missing `routingStrategy`, obsolete
`executionStrategy`, missing or duplicate `operationName`, an operation not
connected to the service, a `FIRST` plan with the wrong step count, and invalid
approve/business/complete order. Plan-shape and operation-resolution errors
include service, inbound action, strategy, step index, role, operation name,
and reason; JSON extraction errors identify the service, inbound definition,
definition, and invalid field available at that point.

For another task-oriented service: define provider-backed operations, connect
them to the service, add one inbound definition per action, choose `FIRST` or
`CHAIN_ON_APPROVE`, order steps in the JSON array, add policies only where
default success is insufficient, and verify stopping, retry, idempotency, and
per-step spans.

Today the seal user manually executes the send task. A future internal trigger
may execute the same `approve_and_execute` plan after the final approval
commits. It must reuse this plan and must not run external NAB work inside the
approval transaction.
