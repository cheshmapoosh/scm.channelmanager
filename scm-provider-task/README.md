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
  -> sets fixed task-workflow role for the matched inbound URL
Service layer
  -> validates the selected service is TASK_WORKFLOW
  -> selects the connected ServiceOperation/Operation for that role
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
