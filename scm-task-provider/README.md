# scm-task-provider

## Purpose

`scm-task-provider` is the internal provider for task and process operations.
It owns task/process entities, repositories, management services, payload models,
and JavaService operation endpoints.

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
ir.daneshrefah.scm.task.autoconfigure.ScmTaskProviderAutoConfiguration
```

from:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

The auto-configuration uses focused scanning for task provider APIs, services,
mappers, repositories, and entities. A TODO remains to replace focused component
scanning with explicit bean registration as the provider surface stabilizes.

The Java package remains `ir.daneshrefah.scm.task` for compatibility.

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

`scm-core` calls provider operations through the standard operation route:

```text
direct:op.<normalized-operation-name>
```

For example:

```text
SVC_CARTABLE_APPROVE_PROCESS
  -> direct:op.SVC_CARTABLE_APPROVE_PROCESS
```

The `TASK_WORKFLOW` handler never injects or directly calls task management or
task API services.

## Host usage

The host includes `scm-task-provider` when task/process operations are required.
The current runtime receives it through the `scm-core` project dependency.

Observation is optional and belongs to the host:

```text
scm-web / host
  -> scm-core
  -> scm-task-provider

scm-web / host
  -> scm-observation-starter   (only when observation is required)
```

`scm-task-provider` does not depend on `scm-observation-starter`.

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
payloads, and calls operation routes. `BUSINESS_OPERATION` must resolve to
exactly one active operation.

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
business failure completes it with `FAIL`. Timeout, connection loss, or another
ambiguous result never marks the process as failed.
