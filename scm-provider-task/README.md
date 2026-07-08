# scm-provider-task

## 1. Purpose

`scm-provider-task` exposes task and process workflow operations through the
`scm-task` Camel component. It is a provider module, not the service router.

```text
Module name:            scm-provider-task
Java package:           ir.daneshrefah.scm.provider.task
Camel component scheme: scm-task
Current providerCode:   internal
Current provider URI:   scm-task:internal
Current engine-type:    internal
```

The module owns the internal task/process APIs, entities, repositories, DTO
conversion, `TaskWorkflowRole` resolution, and delegation to a configured
workflow engine. It does not decide which business service should use
`TASK_WORKFLOW`.

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

The current stateful implementation is the internal engine. Future engines can
be selected by configuration and can own or delegate the same semantic workflow
roles.

```text
Camel component/endpoint
  -> providerCode
  -> provider config
  -> engine-type
  -> TaskWorkflowRole
  -> TaskWorkflowEngine
```

## 3. Current provider: scm-task:internal

The current provider is:

```text
OperationProvider.name = TASK_INTERNAL
OperationProvider.uri  = scm-task:internal
providerCode           = internal
engine-type            = internal
```

The URI shape is:

```text
scm-task:<providerCode>
```

For the current implementation, `scm-task:internal` is canonical. The endpoint
segment is `providerCode`; it is not a task operation name.

## 4. Provider engine configuration

Provider code to engine selection is configuration-based:

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

If no `providers` map is configured, `internal -> internal` is used as the
default provider mapping for backward-compatible startup.

Future providers are modeled the same way, but their engines are not
implemented yet:

```yaml
scm:
  provider:
    task:
      providers:
        internal:
          enabled: true
          engine-type: internal
        camunda:
          enabled: false
          engine-type: camunda
        spring-statemachine:
          enabled: false
          engine-type: spring-statemachine
        flowable:
          enabled: false
          engine-type: flowable
        legacy-cm:
          enabled: false
          engine-type: legacy-cm
```

Only `internal` engine-type is implemented now. Unknown `providerCode` fails
fast when the endpoint is created or used. Unknown enabled `engine-type` fails
fast during provider configuration validation. Disabled future provider entries
are configuration placeholders only.

## 5. Semantic dispatch: TaskWorkflowRole

`TaskWorkflowRole` is the semantic dispatch contract inside
`scm-provider-task`:

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

The internal engine switches on `TaskWorkflowRole`, not on
`SVC_CARTABLE_*` operation codes.

Role resolution order:

```text
1. Exchange property Message.TASK_WORKFLOW_ROLE
2. Exchange property scmTaskWorkflowRole
3. ServiceOperation or Operation definition metadata field taskWorkflowRole
4. Operation.name / Message.OPERATION_NAME when already a TaskWorkflowRole
5. Deprecated SVC_CARTABLE_* operation-code alias
```

If the role is missing, the provider fails fast:

```text
scm-task:<providerCode> requires TaskWorkflowRole from exchange context or operation metadata.
```

## 6. Deprecated SVC_CARTABLE aliases

SVC_CARTABLE_* names are legacy operation-code aliases kept for compatibility.
New TASK_WORKFLOW configuration should use TaskWorkflowRole as the semantic role
and route to scm-task:internal.

Deprecated alias mapping:

```text
SVC_CARTABLE_START_PROCESS              -> START_PROCESS
SVC_CARTABLE_APPROVE_PROCESS            -> APPROVE_PROCESS
SVC_CARTABLE_COMPLETE_PROCESS           -> COMPLETE_PROCESS
SVC_CARTABLE_CANCEL_PROCESS             -> CANCEL_PROCESS
SVC_CARTABLE_COMPLTE_TASK               -> COMPLETE_TASK
SVC_CARTABLE_GET_ALL_TASK               -> FIND_ALL_TASK
SVC_CARTABLE_GET_ALL_PROCESS            -> FIND_ALL_PROCESS
SVC_CARTABLE_GET_TASK_BY_PROCESS_ID     -> FIND_TASK_BY_PROCESS_ID
SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION -> UPDATE_PROCESS_DESCRIPTION
```

The existing spelling `SVC_CARTABLE_COMPLTE_TASK` is preserved as a deprecated
alias. Do not remove these aliases until all old database rows and routes have
migrated.

Old provider URI forms such as `scm-task:SVC_CARTABLE_START_PROCESS` are not
canonical. They are accepted only as deprecated compatibility for old route
startup; runtime dispatch still resolves a `TaskWorkflowRole` from exchange
context or metadata.

## 7. OperationProvider and Operation rows

Define one `OperationProvider` per workflow engine/provider:

```text
OperationProvider.name  = TASK_INTERNAL
OperationProvider.title = SCM Internal Task Provider
OperationProvider.uri   = scm-task:internal
```

Define many `Operation` rows under the same provider. The provider endpoint is
the engine boundary; the semantic action is `TaskWorkflowRole`.

```text
Operation.type     = PROVIDER
Operation.provider = TASK_INTERNAL
```

Do not split task workflow actions into separate `OperationProvider` rows.

## 8. Future engines

Future workflow engines can be added by registering a provider config and a
`TaskWorkflowEngine` implementation:

```text
TASK_CAMUNDA             -> providerCode camunda             -> scm-task:camunda
TASK_SPRING_STATEMACHINE -> providerCode spring-statemachine -> scm-task:spring-statemachine
TASK_FLOWABLE            -> providerCode flowable            -> scm-task:flowable
TASK_LEGACY_CM           -> providerCode legacy-cm          -> scm-task:legacy-cm
```

The extension rule remains:

```text
one provider per engine
many operations per provider
TaskWorkflowRole resolved from exchange context or metadata
```

This module does not implement Camunda, Spring StateMachine, Flowable, or
legacy CM yet.

## 9. Boundary with scm-web

`scm-web` decides what service and operation should run. `scm-provider-task`
executes the workflow/state-machine operation.

```text
scm-web / routing
  -> resolves Service
  -> applies RoutingStrategy = TASK_WORKFLOW
  -> selects Operation
  -> sets Message.TASK_WORKFLOW_ROLE or operation metadata
  -> routes to OperationProvider.uri = scm-task:internal

scm-provider-task
  -> validates providerCode
  -> resolves configured engine-type
  -> resolves TaskWorkflowRole
  -> dispatches to TaskWorkflowEngine
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
Safe fields include correlation id, process id, task id, process code, status,
outcome, command, role, operation name, and sanitized error type/message.

Never log or publish OTP, authorization token, password, raw financial payload,
or full request/response bodies that may contain sensitive data.
