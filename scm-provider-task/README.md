# scm-provider-task

## Purpose

`scm-provider-task` is the internal provider for task and process operations.
It owns task/process entities, repositories, management services, payload models,
JavaService operations, and the `scm-task:` Camel endpoint.

```text
Module name:            scm-provider-task
Java package:           ir.daneshrefah.scm.provider.task
Camel component scheme: scm-task
Operation route:        direct:op.<operationName>
Provider endpoint:      scm-task:<operationName>
```

It is a provider rather than a workflow router:

- It implements task/process business rules.
- It exposes operations through the existing JavaService mechanism.
- It does not create gateway routes.
- It does not interpret `TASK_WORKFLOW` command definitions.
- It does not orchestrate business operations.
- It does not call observation APIs directly.

`scm-core` owns routing and orchestration. The host application owns observation
policy and public exposure.

## Auto-configuration

Spring Boot loads:

```text
ir.daneshrefah.scm.provider.task.autoconfigure.ScmTaskProviderAutoConfiguration
```

from:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

The host enables the entire provider with one property:

```yaml
scm:
  provider:
    task:
      enabled: true
```

There is no secondary persistence enablement switch. If
`scm.provider.task.enabled` is missing or false, the provider does not register
its API/service beans, `scm-task` component, operation adapter, repositories, or
JPA managed package contribution.

The auto-configuration uses focused scanning for task provider APIs, services,
and mappers. Persistence has two supported modes.

Default mode uses the host primary persistence beans:

```yaml
scm:
  provider:
    task:
      enabled: true
```

In default mode, task provider repositories bind to:

```text
entityManagerFactory
transactionManager
```

The task provider entity package may be contributed to the host main
`entityManagerFactory` through `JpaManagedPackageContributor`. The provider does
not create a datasource, entity manager factory, or transaction manager.

Dedicated mode uses configured bean names:

```yaml
scm:
  provider:
    task:
      enabled: true
      datasource: taskProviderDataSource
      entity-manager-factory: taskProviderEntityManagerFactory
      transaction-manager: taskProviderTransactionManager
```

In dedicated mode:

- `datasource` is the bean name of the task provider datasource.
- `entity-manager-factory` is the bean name of the task provider
  `EntityManagerFactory`.
- `transaction-manager` is the bean name of the task provider
  `TransactionManager`.
- Task provider repositories bind to `taskProviderEntityManagerFactory` and
  `taskProviderTransactionManager`.
- The task provider does not use host primary persistence and does not
  contribute `ir.daneshrefah.scm.provider.task.entity` to the host main
  `entityManagerFactory`.

If none of `datasource`, `entity-manager-factory`, and `transaction-manager` is
configured, default mode is used. If any one is configured, all three are
required and the named beans must exist at startup. A TODO remains to replace
focused component scanning with explicit bean registration as the provider
surface stabilizes.

The provider Java package is `ir.daneshrefah.scm.provider.task`.

## JavaService operations

The provider keeps the existing direct operation contracts available:

- `SVC_CARTABLE_START_PROCESS`
- `SVC_CARTABLE_GET_ALL_PROCESS`
- `SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION`
- `SVC_CARTABLE_CANCEL_PROCESS`
- `SVC_CARTABLE_COMPLETE_PROCESS`
- `SVC_CARTABLE_APPROVE_PROCESS`
- `SVC_CARTABLE_GET_ALL_TASK`
- `SVC_CARTABLE_COMPLTE_TASK`
- `SVC_CARTABLE_GET_TASK_BY_PROCESS_ID`

Existing direct `FIRST` configuration remains compatible. Public command-style
exposure should be deprecated through configuration where appropriate; provider
operations are not deleted to enforce that policy.

## Operation routes

`ServiceOperation.operationName` in the database is the operation code. Do not
store the generated `op.` prefix in this field:

```text
operationName = SVC_CARTABLE_APPROVE_PROCESS
```

`scm-core` generates the route id and endpoint with
`RouteIdSupport.operationRouteId(operationName)`:

```text
DB Operation.name:              SVC_CARTABLE_APPROVE_PROCESS
ServiceOperation.operationName: SVC_CARTABLE_APPROVE_PROCESS
Generated operation route id:   op.SVC_CARTABLE_APPROVE_PROCESS
Operation route endpoint:       direct:op.SVC_CARTABLE_APPROVE_PROCESS
Task provider endpoint:         scm-task:SVC_CARTABLE_APPROVE_PROCESS
```

For provider URI `scm-task:`, `Operation.name` must exactly match one of the
supported task `OperationCode` values, including existing spellings such as
`SVC_CARTABLE_COMPLTE_TASK`. Leading/trailing whitespace, unknown codes, and an
`op.` prefix fail endpoint creation during route startup.

Configure each task `Operation` with `type = PROVIDER` and bind it to the task
operation provider whose base URI is `scm-task:`. The generic provider
operation handler appends the operation code:

```text
direct:op.SVC_CARTABLE_APPROVE_PROCESS
  -> scm-task:SVC_CARTABLE_APPROVE_PROCESS
  -> ProcessInstanceService.approve(...)
```

Supported provider endpoints include:

```text
scm-task:SVC_CARTABLE_START_PROCESS
scm-task:SVC_CARTABLE_APPROVE_PROCESS
scm-task:SVC_CARTABLE_COMPLETE_PROCESS
scm-task:SVC_CARTABLE_CANCEL_PROCESS
scm-task:SVC_CARTABLE_COMPLTE_TASK
scm-task:SVC_CARTABLE_GET_ALL_TASK
scm-task:SVC_CARTABLE_GET_ALL_PROCESS
scm-task:SVC_CARTABLE_GET_TASK_BY_PROCESS_ID
scm-task:SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION
```

The `TASK_WORKFLOW` handler calls only `direct:op.*` routes and never injects or
directly calls task management or task API services. `scm-core` creates generic
`Map`/`JsonNode` payloads. `TaskProviderOperationAdapter` owns conversion to
task-provider DTOs and invokes the appropriate provider API bean.

## Host usage

The host includes `scm-provider-task` when task/process operations are required.
`scm-core` does not depend on the provider module; the running host selects it.

Observation is optional and belongs to the host:

```text
scm-web / host
  -> scm-core
  -> scm-provider-task (when task operations are enabled)

scm-web / host
  -> scm-observation-starter   (only when observation is required)
```

`scm-provider-task` does not depend on `scm-observation-starter`.

## Events and observation

Task/process lifecycle events are published through the existing
`scm-common` event provider contract:

```text
ir.daneshrefah.scm.common.event.provider
```

`TaskProviderEventPublisher` publishes `ScmProviderEvent` through
`ScmEventPublisher`. Safe process/task identifiers and status metadata may be
included. Request bodies and financial payloads are not included.

The host listens for provider events and decides whether each event becomes:

- application log
- trace event
- audit event
- metric

When no common event publisher or observation implementation is available,
publication/observation is a no-op. The provider does not choose an observation
signal itself.

## Logging

SLF4J is allowed for technical diagnostics with useful boundary context.
Do not add repeated catch/log/rethrow blocks that only repeat an exception
message. Semantic lifecycle boundaries should use the common event provider.

Safe fields include:

- correlation id
- process id
- task id
- process code
- status
- outcome
- command
- role
- operation name
- sanitized and truncated error type/message

Never log or publish:

- OTP
- authorization token
- password
- raw financial payload
- full request or response body that may contain sensitive data

## TASK_WORKFLOW configuration

Gateway configuration and operation configuration have separate purposes.

Each inbound command has its own `INBOUND` row and Definition. The inbound
Definition declares the requested command and its ordered steps:

Every `INBOUND` row must also define an explicit, non-blank REST `path`, for
example `/fund-transfer/task-workflow/processes/{processId}/approve`. Blank paths
are invalid and fail route construction; there is no backward-compatible path
fallback.

```json
{
  "inboundAction": "APPROVE_AND_EXECUTE",
  "taskWorkflow": {
    "steps": [
      {"role": "APPROVE_PROCESS", "executionOrder": 10},
      {"role": "BUSINESS_OPERATION", "executionOrder": 20},
      {"role": "COMPLETE_PROCESS", "executionOrder": 30}
    ]
  }
}
```

Each active `ServiceOperation` Definition declares only its role:

```json
{
  "taskWorkflowRole": "BUSINESS_OPERATION"
}
```

Do not put command mappings in `ServiceOperation.definition.details`.
The inbound Definition describes command flow; the operation Definition
describes the role of one operation.

The gateway binds only:

```text
Message.INBOUND_ROUTE_ACTION
Message.INBOUND_PATH_VARIABLES
```

The service layer resolves the command, selects the prebuilt command plan, maps
generic payloads, and calls operation routes. DTO conversion occurs only in
`scm-provider-task`. `BUSINESS_OPERATION` must resolve to exactly one active
operation.

Examples:

```json
{
  "inboundAction": "START",
  "taskWorkflow": {
    "steps": [
      {"role": "START_PROCESS", "executionOrder": 10}
    ]
  }
}
```

```json
{
  "inboundAction": "COMPLETE_TASK",
  "taskWorkflow": {
    "steps": [
      {"role": "COMPLETE_TASK", "executionOrder": 10}
    ]
  }
}
```

## Flow summary

```text
START:
  inbound command -> START_PROCESS operation

COMPLETE_TASK:
  inbound command -> COMPLETE_TASK operation

APPROVE_AND_EXECUTE:
  APPROVE_PROCESS -> BUSINESS_OPERATION -> COMPLETE_PROCESS

UNKNOWN business result:
  do not call COMPLETE_PROCESS
  do not mark the process as FAIL
  leave the process for recovery/reconciliation
```

For `APPROVE_AND_EXECUTE`, the approve response is retained in exchange
properties. Its `transactionData` is used as the preferred business operation
payload. Definitive success completes the process with `COMPLETE`; definitive
business failure completes it with `FAIL`. If a definitive business exception
was thrown and that completion succeeds, the workflow returns a non-retryable
failure response instead of rethrowing the original exception. This prevents an
upstream retry after the process is already terminal. If that `FAIL` completion
itself fails, the completion failure is propagated with the original definitive
business failure attached as suppressed diagnostic context. Timeout, connection
loss, or another ambiguous result does not call `COMPLETE_PROCESS`, publishes
the unknown-result semantic event, and propagates an unknown-result error for
recovery/reconciliation.
