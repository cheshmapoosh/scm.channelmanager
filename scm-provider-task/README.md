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
Message.TASK_WORKFLOW_STEP_TYPE = <TaskWorkflowStepType>
```

`providerCode` selects the configured task engine.
`ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType` is the
shared, type-safe semantic step contract used by both `scm-core` and this
provider.

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

## TaskWorkflowStepType

The provider requires exactly one typed exchange property:

```text
Message.TASK_WORKFLOW_STEP_TYPE = TaskWorkflowStepType
```

Strings, unrelated enums, and objects converted through `valueOf()` are not
accepted at the provider boundary. Supported step types are:

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

`BUSINESS_OPERATION` is intentionally not supported by `scm-task`. Startup
plan validation rejects a business step connected to an `scm-task` provider,
and the provider repeats the capability check defensively at runtime.

If the typed step property is missing or has the wrong type, the provider fails
before engine execution:

```text
scm-task:<providerCode> requires Message.TASK_WORKFLOW_STEP_TYPE as TaskWorkflowStepType.
```

The provider does not derive step types from service records, operation records,
provider URIs, or legacy cartable operation-code names.

## Runtime Boundary

Runtime flow:

```text
Shared TASK_WORKFLOW InboundChannelServiceDefinition
  -> GatewayChannelLayerRouteBuilder
  -> GatewayInboundRouteFactory
  -> GatewayRoutePipelineConfigurer
  -> TaskWorkflowRouteIdentityResolver supplies serviceCode and inboundAction
Service layer
  -> resolves serviceCode + inboundAction to a service-owned ActionPlan
  -> selects the shared FIRST or CHAIN_ON_APPROVE engine
  -> executes configured operations by operationName
Operation layer
  -> routes to Operation.provider.uri
scm-provider-task
  -> validates providerCode
  -> validates the typed TaskWorkflowStepType capability
  -> executes the supported task/process step on the configured engine
```

`scm-provider-task` does not decide which business service should use task
workflow. It does not parse service codes, inbound actions, ActionPlan JSON, or
gateway paths. The shared TASK_WORKFLOW category uses the existing generic
gateway pipeline; this provider does not add a gateway route builder.

Task and process identifiers arrive through normalized SCM input. A REST adapter
normalizes configured path variables into `Message.INBOUND_PARAMETERS`; ISO,
MQ, Kafka, SOAP, and other adapters may populate that same map or the canonical
payload. This provider does not parse REST paths, HTTP fields, or any other
protocol-specific identifier source.

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

The provider also supplies the mandatory durable recovery store. It writes the
versioned workflow snapshot through the unchanged
`TBL_PRC_PROCESS_INSTANCE_WATCHER` mapping with:

```text
TYPE   = WORKFLOW_EXECUTION
ROW_NO = 0
```

Process correlation remains backward compatible. A workflow start initializes
the existing process correlation from `Message.EXECUTION_ID`. Approve preserves
that value. For a legacy process whose stored correlation is null or blank,
approve still requires and stores the request `correlationId`.

## Events and Observation

Task/process lifecycle events are published through the existing
`scm-common` event provider contract:

```text
ir.daneshrefah.scm.common.event.provider
```

Safe process/task identifiers and status metadata may be published. Sensitive
request data must not be logged, traced, audited, or emitted as metric tags.

## TASK_WORKFLOW service definitions

This README documents provider behavior only:

```text
scm-provider-task/README.md
    -> documents provider behavior

scm-core/docs/task-workflow-service.md
    -> documents service and inbound-route configuration
```

For service routing, inbound route configuration, database relationships,
identifier normalization, command JSON, validation, examples, and
troubleshooting, use the canonical guide:

[Defining TASK_WORKFLOW services](../scm-core/docs/task-workflow-service.md)

Provider-specific reminders:

1. `scm-task:<providerCode>` requires
   `Message.TASK_WORKFLOW_STEP_TYPE = TaskWorkflowStepType`.
2. The internal provider supports task/process step types listed above.
3. `BUSINESS_OPERATION` must be routed to a business provider, not to
   `scm-task`.
4. The provider does not parse gateway routes, REST path variables, service
   routing strategy, or inbound actions.
