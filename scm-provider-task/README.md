# scm-provider-task

## 1. Purpose

`scm-provider-task` exposes task and process workflow operations through the
`scm-task` Camel component.

This module is a provider module. It executes task/workflow operations after a
caller has already selected the target `Operation` and routed an exchange to the
provider endpoint.

```text
Module name:            scm-provider-task
Java package:           ir.daneshrefah.scm.provider.task
Camel component scheme: scm-task
Current providerCode:   internal
Current provider URI:   scm-task:internal
```

The module owns the internal task/process APIs, entities, repositories, DTO
conversion, and dispatch from `operationCode` to the existing task services. It
does not decide which business service should use `TASK_WORKFLOW`.

Spring Boot loads:

```text
ir.daneshrefah.scm.provider.task.autoconfigure.ScmTaskProviderAutoConfiguration
```

The host enables the provider with:

```yaml
scm:
  provider:
    task:
      enabled: true
```

## 2. Concept: wrapper around stateful workflow/state-machine engines

`scm-provider-task` is a stateful workflow/state-machine wrapper. The provider
owns or delegates workflow state transitions such as start, approve, complete,
cancel, and task completion.

The state may currently be managed by the internal implementation. In the
future, the same provider component can delegate to engines such as Camunda,
Spring StateMachine, Flowable, or legacy CM.

The wrapper boundary is:

```text
Camel component/endpoint
  -> providerCode selection
  -> operationCode resolution
  -> task provider dispatch
  -> workflow/state-machine engine
```

## 3. Current provider: scm-task:internal

The current engine/provider is `internal`.

```text
Provider URI: scm-task:internal
providerCode: internal
```

The URI shape is:

```text
scm-task:<providerCode>
```

For the current implementation, `scm-task:internal` is canonical. The endpoint
segment is the `providerCode`; it is not the task operation name.

Runtime relationship:

```text
Operation route
  -> OperationProvider.uri = scm-task:internal
  -> TaskProviderComponent
  -> TaskProviderEndpoint(providerCode=internal)
  -> TaskProviderProducer
  -> TaskProviderOperationAdapter
  -> ProcessInstanceService / TaskInstanceService
```

The internal dispatcher still executes the existing task services. There is no
Camunda, Spring StateMachine, Flowable, or legacy CM implementation in this
module yet.

## 4. Why the provider is stateful

Task workflow operations are not simple stateless calls. They change or depend
on process/task state:

- `START_PROCESS` creates process state.
- `APPROVE_PROCESS` advances an approval state.
- `COMPLETE_PROCESS` moves a process into a terminal state.
- `CANCEL_PROCESS` cancels an active process.
- `COMPLETE_TASK` changes task state.
- Query operations read the current process/task state.

The internal implementation stores and validates these transitions through the
existing process and task services. Future engines may own the state directly or
the provider may delegate state changes to them.

## 5. How operationCode is resolved

With canonical URI `scm-task:internal`, the provider resolves the
`operationCode` from the `Exchange` at runtime:

```text
1. Message.OPERATION_NAME
2. Message.OPERATION.name
3. legacy URI operationCode, only for scm-task:SVC_CARTABLE_*
```

If the `operationCode` is missing, the provider fails fast with a clear error.
If the `providerCode` is unknown, endpoint creation fails fast with a clear
error. If the `operationCode` is not one of the supported internal operations,
the provider fails fast with the supported operation list.

Backward-compatible legacy URI form:

```text
scm-task:SVC_CARTABLE_START_PROCESS
```

is interpreted as:

```text
providerCode  = internal
operationCode = SVC_CARTABLE_START_PROCESS
```

This compatibility exists only for old `scm-task:SVC_CARTABLE_*` provider URIs.
New configuration should use `scm-task:internal`.

## 6. Supported internal operations

The current internal provider supports these `operationCode` values:

```text
SVC_CARTABLE_START_PROCESS
SVC_CARTABLE_APPROVE_PROCESS
SVC_CARTABLE_COMPLETE_PROCESS
SVC_CARTABLE_CANCEL_PROCESS
SVC_CARTABLE_COMPLTE_TASK
SVC_CARTABLE_GET_ALL_TASK
SVC_CARTABLE_GET_ALL_PROCESS
SVC_CARTABLE_GET_TASK_BY_PROCESS_ID
SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION
```

The existing spelling `SVC_CARTABLE_COMPLTE_TASK` is preserved for
compatibility.

## 7. How to configure OperationProvider for internal engine

Define one `OperationProvider` for the internal workflow engine:

```text
OperationProvider.name  = TASK_INTERNAL
OperationProvider.title = SCM Internal Task Provider
OperationProvider.uri   = scm-task:internal
providerCode            = internal
```

Then define many `Operation` rows under the same provider:

```text
SVC_CARTABLE_START_PROCESS              -> provider TASK_INTERNAL
SVC_CARTABLE_APPROVE_PROCESS            -> provider TASK_INTERNAL
SVC_CARTABLE_COMPLETE_PROCESS           -> provider TASK_INTERNAL
SVC_CARTABLE_CANCEL_PROCESS             -> provider TASK_INTERNAL
SVC_CARTABLE_COMPLTE_TASK               -> provider TASK_INTERNAL
SVC_CARTABLE_GET_ALL_TASK               -> provider TASK_INTERNAL
SVC_CARTABLE_GET_ALL_PROCESS            -> provider TASK_INTERNAL
SVC_CARTABLE_GET_TASK_BY_PROCESS_ID     -> provider TASK_INTERNAL
SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION -> provider TASK_INTERNAL
```

Each task `Operation` should use:

```text
Operation.type     = PROVIDER
Operation.provider = TASK_INTERNAL
Operation.name     = <operationCode>
```

Define one provider per engine and many operations per provider. Do not split
task workflow operations into separate `OperationProvider` rows.

Legacy configuration with provider URI `scm-task:` may still route to
`scm-task:SVC_CARTABLE_*` because the generic provider handler appends
`Operation.name` when a provider URI ends with `:`. Keep that only for backward
compatibility; new task workflow configuration should use `scm-task:internal`.

## 8. How future engines can be added

Future workflow engines can be added by registering a new `providerCode` and
delegating from the same `scm-task` component to the new engine implementation.

Possible future `OperationProvider` rows:

```text
TASK_CAMUNDA             -> scm-task:camunda
TASK_SPRING_STATEMACHINE -> scm-task:spring-statemachine
TASK_FLOWABLE            -> scm-task:flowable
TASK_LEGACY_CM           -> scm-task:legacy-cm
```

The extension rule remains the same:

```text
one provider per engine
many operations per provider
operationCode resolved from the Exchange
```

Unknown `providerCode` values fail fast until an engine implementation is
registered. This README documents the extension point only; it does not add a
future engine implementation.

## 9. Boundary with scm-web

`scm-web` decides what service and operation should run. `scm-provider-task`
executes the workflow/state-machine operation.

`scm-provider-task` does not decide which business service should use
`TASK_WORKFLOW`. It only executes task/workflow operations after
`scm-web`/routing sends an exchange to the task provider endpoint.

Keep the boundary clear:

```text
scm-web / routing
  -> resolves Service
  -> applies RoutingStrategy = TASK_WORKFLOW
  -> selects Operation
  -> sets Message.OPERATION and/or Message.OPERATION_NAME
  -> routes to OperationProvider.uri = scm-task:internal

scm-provider-task
  -> validates providerCode = internal
  -> resolves operationCode
  -> dispatches to the internal workflow/state-machine implementation
```

`scm-provider-task` should not document business service routing rules except
as context. `scm-web` should not document internal task engine details.

## Auto-configuration and persistence

Spring Boot loads the provider auto-configuration from:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
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
not create a datasource, `EntityManagerFactory`, or `TransactionManager`.

Dedicated mode uses configured bean names:

```yaml
scm:
  provider:
    task:
      enabled: true
      entity-manager-factory: taskProviderEntityManagerFactory
      transaction-manager: taskProviderTransactionManager
```

In dedicated mode:

- `entity-manager-factory` is the bean name of the task provider
  `EntityManagerFactory`.
- `transaction-manager` is the bean name of the task provider
  `TransactionManager`.
- Task provider repositories bind to `taskProviderEntityManagerFactory` and
  `taskProviderTransactionManager`.
- The task provider does not use host primary persistence and does not
  contribute `ir.daneshrefah.scm.provider.task.entity` to the host main
  `entityManagerFactory`.
- The host creates any dedicated datasource and wires it into the dedicated
  `EntityManagerFactory`.

If neither `entity-manager-factory` nor `transaction-manager` is configured,
default mode is used. If either one is configured, both are required and the
named beans must exist at startup.

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
