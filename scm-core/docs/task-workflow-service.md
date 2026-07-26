# Defining TASK_WORKFLOW services

This is the canonical guide for configuring and running SCM services whose
service routing strategy is `TASK_WORKFLOW`.

`TASK_WORKFLOW` is a service-layer routing strategy. It is not a gateway type
and it does not have a dedicated gateway route builder.

```text
Protocol-specific gateway
    -> inbound route matching
    -> RequestContractDecoder
    -> normalized payload and inbound parameters
    -> GatewayRoutePipelineConfigurer
    -> service route
    -> TASK_WORKFLOW inboundAction resolution
    -> command plan
    -> FIRST or CHAIN_ON_APPROVE
    -> ordered operation routes
    -> operation providers
```

TASK_WORKFLOW inbound routes use the same generic gateway pipeline as ordinary
services. There is no separate TASK_WORKFLOW gateway route builder, service
entrypoint route, direct service-entrypoint URI, or fixed TASK_WORKFLOW endpoint
family.

## Configuration boundaries

There are two separate contracts.

### Inbound route definition

The route exposed to a protocol adapter is represented by
`InboundChannelServiceDefinition`. It contains gateway-facing route information:

```text
type
method
path
definition
gatewayChannel
channelServiceAccess
```

For REST, `method` and `path` belong to the inbound channel service definition:

```text
type   = INBOUND
method = POST
path   = /v1/paymasters/tasks/{taskId}/complete
```

These values define how the gateway receives the request. They do not select a
workflow operation.

Current persistence note: `TBL_SCM_CHN_SVC_DEFINITION` has no verified
`METHOD` or `PATH` columns. The current `ChannelServiceDefinitionMapper`
hydrates `InboundChannelServiceDefinition.method` and
`InboundChannelServiceDefinition.path` from top-level `method` and `path`
values in the linked `Definition.details`. Treat that as the current mapping
mechanism for the inbound route model, not as fields in the TASK_WORKFLOW
command contract.

### TASK_WORKFLOW command definition

The linked `Definition.details` also contains the TASK_WORKFLOW command JSON
consumed by the service layer:

```json
{
  "inboundAction": "task_complete",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "COMPLETE_TASK",
        "operationName": "complete-paymaster-approval-task"
      }
    ]
  }
}
```

Keep the boundary clear:

```text
method/path
    -> define how the gateway receives the request

inboundAction/taskWorkflow
    -> define how the service layer executes the request
```

## Full definition checklist

Configure records in this order:

1. Define or select the required `OperationProvider` records.
2. Define the task-provider operations.
3. Define the business operations.
4. Connect each operation to its provider.
5. Create active `ServiceOperation` connections.
6. Create the service.
7. Set the service routing strategy to `TASK_WORKFLOW`.
8. Connect the service to the target channel through `ChannelServiceAccess`.
9. Select the target `GatewayChannel`.
10. Create one `InboundChannelServiceDefinition` for every external action.
11. Set the inbound `method` and `path` for REST routes.
12. Create a separate linked `Definition`.
13. Put `inboundAction` and `taskWorkflow` JSON in `Definition.details`.
14. Confirm every configured `operationName` refers to an active operation
    connected to the same service.
15. Confirm the task provider supports every non-business `stepType` routed to
    `scm-task`.
16. Start the application and resolve all startup plan-validation errors.

## Database relationships

Conceptual model:

```text
Service
  -> ChannelServiceAccess
      -> ChannelServiceDefinition
          -> GatewayChannel
          -> Definition

Service
  -> ServiceOperation
      -> Operation
          -> OperationProvider
```

### `REF.EB_SERVICE`

Purpose: business service record.

Important identifiers:

```text
EB_SERVICE_ID
CODE
NAME
```

Relevant fields:

```text
PUBLISH
ROUTING_STRATEGY
SERVICE_CATEGORY_ID
```

TASK_WORKFLOW value:

```text
ROUTING_STRATEGY = TASK_WORKFLOW
```

Scope: shared by all inbound actions for the service.

### `REF.CHANNEL_SERVICE_ACCESS`

Purpose: makes one service available to one channel.

Important identifiers:

```text
CHANNEL_SERVICE_ACCESS_ID
```

Relevant foreign keys:

```text
CHANNEL_ID
EB_SERVICE_ID
```

TASK_WORKFLOW value:

```text
ACTIVE = true
```

Scope: usually shared by all inbound actions for that service/channel exposure.

### `REF.TBL_SCM_CHN_SVC_DEFINITION`

Purpose: connects a channel-service access and a gateway channel to a route
definition.

Important identifiers:

```text
CHN_SVC_DEFINITION_ID
```

Relevant foreign keys:

```text
CHANNEL_SERVICE_ACCESS_ID
GATEWAY_CHANNEL_ID
DEFINITION_ID
```

Relevant verified fields:

```text
TYPE
```

TASK_WORKFLOW value:

```text
TYPE = INBOUND
```

Scope: one record per external inbound action. Multiple routes must be modeled
as multiple `INBOUND` definitions.

No verified `METHOD` or `PATH` columns exist on this table. In the current
mapper, REST route fields are populated indirectly from the linked
`Definition.details`.

### `REF.TBL_SCM_DEFINITION`

Purpose: stores named route/configuration details.

Important identifiers:

```text
DEFINITION_ID
NAME
```

Relevant fields:

```text
TITLE
ENGINE
DETAILS
```

TASK_WORKFLOW value: `DETAILS` must contain valid JSON with `inboundAction` and
`taskWorkflow`. For REST inbound definitions, the same linked definition is
also the current source used by the mapper to hydrate the inbound route
`method` and `path`.

Scope: one definition per inbound action.

### `REF.TBL_SCM_SERVICE_OPERATION`

Purpose: connects a service to the operation names it may execute.

Important identifiers:

```text
SERVICE_OPERATION_ID
```

Relevant foreign keys:

```text
EB_SERVICE_ID
DEFINITION_ID
```

Relevant fields:

```text
ACTIVE
OPERATION_NAME
```

TASK_WORKFLOW value: every `operationName` in a command plan must match one
active `ServiceOperation.operationName` connected to the same service.

Scope: shared by any inbound action that names the operation.

### `REF.TBL_SCM_OPERATION`

Purpose: defines an executable operation and its provider binding.

Important identifiers:

```text
OPERATION_ID
NAME
```

Relevant foreign keys:

```text
OPERATION_PROVIDER_ID
```

Relevant fields:

```text
ACTIVE
TYPE
PATH
```

TASK_WORKFLOW value: the operation named by `ServiceOperation.operationName`
must exist and be active. Task/process steps usually target a `PROVIDER`
operation whose provider URI starts with `scm-task:`.

Scope: shared by all services that connect to the same operation.

### `REF.TBL_SCM_OPERATION_PROVIDER`

Purpose: defines the concrete provider endpoint.

Important identifiers:

```text
OPERATION_PROVIDER_ID
NAME
```

Relevant fields:

```text
URI
ACTIVE
```

TASK_WORKFLOW value for the internal task provider:

```text
URI = scm-task:internal
ACTIVE = true
```

Scope: shared by operations.

### `REF.TBL_SCM_GATEWAY_CHANNEL`

Purpose: defines the runtime gateway channel and protocol.

Important identifiers:

```text
GATEWAY_CHANNEL_ID
NAME
```

Relevant fields:

```text
ACTIVE
PROTOCOL_TYPE
HOST
PORT
PATH
CHANNEL_ID
```

TASK_WORKFLOW value: must be active and must match the runtime gateway name
being started.

Scope: shared by routes exposed through that gateway.

## TASK_WORKFLOW JSON field reference

| Field | Required | Allowed values | Consumed by | Meaning | Does not control | Startup validation |
| --- | --- | --- | --- | --- | --- | --- |
| `inboundAction` | Yes | `start`, `task_complete`, `complete_task`, `approve_and_execute`, `cancel_process`, `find_processes`, `find_tasks`, `find_tasks_by_process_id`, `update_process_description` | `InboundRouteActionConfigExtractor`, `TaskWorkflowCommandResolver` | Selects the command plan | REST path, HTTP method, operation provider | Required nonblank string; unsupported values fail |
| `taskWorkflow` | Yes | JSON object | `TaskWorkflowInboundCommandConfigExtractor` | Container for execution plan | Gateway route matching | Required object |
| `routingStrategy` | Yes | `FIRST`, `CHAIN_ON_APPROVE` | `TaskWorkflowRoutePlanFactory` | Selects the reusable routing engine for this inbound action | Service routing strategy on `EB_SERVICE` | Required; any other value fails |
| `steps` | Yes | Non-empty array | `TaskWorkflowRoutePlanFactory` | Ordered step list | Gateway route order | Required non-empty array |
| `stepType` | Yes | `TaskWorkflowStepType` enum values | Request mapper, task provider capability checks, observation | Defines semantic meaning of the workflow step | Does not select the operation and does not identify an authorization role | Required; invalid enum fails |
| `operationName` | Yes | Active `ServiceOperation.operationName` connected to the same service | `TaskWorkflowRoutePlanFactory` | Selects the actual `ServiceOperation` | Does not define step semantics | Required; no matching active operation fails |
| `decisionPolicy` | No | Registered `ChainStepDecisionPolicy.code()`, for example `DEFAULT_SUCCESS` or `NAB_PAYMASTER_REGISTRATION` | `ChainStepDecisionPolicyRegistry` | Converts a chain step result into `CONTINUE`, `RETRY_LATER`, or `FAIL` | Does not select operation, provider, or route | If supplied, must be a nonblank string and registered policy code |

Important rules:

```text
inboundAction
    -> selects the command plan

operationName
    -> selects the actual ServiceOperation

stepType
    -> defines the semantic meaning of the workflow step

decisionPolicy
    -> converts a chain step result into CONTINUE, RETRY_LATER, or FAIL
```

`stepType` does not select the operation and does not identify an authorization
role.

Unsupported fields inside a step object fail startup validation. The extractor
currently supports only:

```text
stepType
operationName
decisionPolicy
```

`taskWorkflow.executionStrategy` is explicitly rejected; use
`taskWorkflow.routingStrategy`.

## Supported step types

`TaskWorkflowStepType` is the shared enum:

```text
START_PROCESS
COMPLETE_TASK
APPROVE_PROCESS
BUSINESS_OPERATION
COMPLETE_PROCESS
CANCEL_PROCESS
FIND_ALL_PROCESS
FIND_ALL_TASK
FIND_TASK_BY_PROCESS_ID
UPDATE_PROCESS_DESCRIPTION
```

| Step type | Semantic purpose | Expected input identifiers | Supported by `scm-provider-task` | Direct inbound action | Expected position | Typical operation name |
| --- | --- | --- | --- | --- | --- | --- |
| `START_PROCESS` | Start a process instance | Request payload for process start | Yes | Yes | Single `FIRST` step | `start-paymaster-process` |
| `COMPLETE_TASK` | Complete an approval or work task | `taskId`; decision fields come from payload/transformer contract | Yes | Yes | Single `FIRST` step | `complete-paymaster-approval-task` |
| `APPROVE_PROCESS` | Validate/approve the process before a business execution chain | `processId` | Yes | Not as a standalone final process-completion action; used as chain first step | First step in `CHAIN_ON_APPROVE` | `approve-paymaster-process-for-execution` |
| `BUSINESS_OPERATION` | Execute a business operation between process approval and completion | Current workflow `processId`; optional business payload from transaction data and `stepResults` | No | No in the canonical approve-and-execute flow | Middle steps in `CHAIN_ON_APPROVE` | `register-paymaster-in-nab` |
| `COMPLETE_PROCESS` | Mark the workflow process complete after successful business steps | Current workflow `processId` | Yes | No | Final step in `CHAIN_ON_APPROVE` | `complete-paymaster-process` |
| `CANCEL_PROCESS` | Cancel a process | `processId` | Yes | Yes | Single `FIRST` step | `cancel-paymaster-process` |
| `FIND_ALL_PROCESS` | Query process instances | Filter payload, if any | Yes | Yes | Single `FIRST` step | `find-paymaster-processes` |
| `FIND_ALL_TASK` | Query tasks | Filter payload, if any | Yes | Yes | Single `FIRST` step | `find-paymaster-tasks` |
| `FIND_TASK_BY_PROCESS_ID` | Query tasks belonging to one process | `processId` | Yes | Yes | Single `FIRST` step | `find-paymaster-tasks-by-process-id` |
| `UPDATE_PROCESS_DESCRIPTION` | Update a process description | `processId` plus update payload | Yes | Yes | Single `FIRST` step | `update-paymaster-process-description` |

`BUSINESS_OPERATION` is a workflow orchestration type. It is not an operation
executed by `scm-provider-task`.

## Routing strategies

### `FIRST`

`FIRST` is used for a single operation.

Rules:

```text
exactly one step is required
one operation is executed
no chain decision policy is evaluated
```

Use it for actions such as start, complete task, cancel, and query operations.

Example:

```json
{
  "inboundAction": "task_complete",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "COMPLETE_TASK",
        "operationName": "complete-paymaster-approval-task"
      }
    ]
  }
}
```

### `CHAIN_ON_APPROVE`

`CHAIN_ON_APPROVE` executes ordered steps according to array position.

Rules:

```text
first step  = APPROVE_PROCESS
middle      = one or more BUSINESS_OPERATION steps
final step  = COMPLETE_PROCESS
```

A decision policy may return:

```text
CONTINUE
RETRY_LATER
FAIL
```

Skipped steps do not execute. Earlier successful responses are recorded by
operation name and are available to later business steps through `stepResults`.

Valid complete example:

```json
{
  "inboundAction": "approve_and_execute",
  "taskWorkflow": {
    "routingStrategy": "CHAIN_ON_APPROVE",
    "steps": [
      {
        "stepType": "APPROVE_PROCESS",
        "operationName": "approve-paymaster-process-for-execution"
      },
      {
        "stepType": "BUSINESS_OPERATION",
        "operationName": "register-paymaster-in-nab",
        "decisionPolicy": "NAB_PAYMASTER_REGISTRATION"
      },
      {
        "stepType": "BUSINESS_OPERATION",
        "operationName": "grant-paymaster-account-access-in-scm"
      },
      {
        "stepType": "COMPLETE_PROCESS",
        "operationName": "complete-paymaster-process"
      }
    ]
  }
}
```

## Complete inbound action examples

Examples below show the logical route fields separately from the command JSON.
With the current mapper, REST `method` and `path` are persisted indirectly from
the linked definition details into `InboundChannelServiceDefinition`; keep the
route concern separate from the command concern when designing and reviewing
configuration.

### Start process

Route:

```text
POST /v1/paymasters/processes
```

Configuration:

```json
{
  "inboundAction": "start",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "START_PROCESS",
        "operationName": "start-paymaster-process"
      }
    ]
  }
}
```

### Complete approval task

Route:

```text
POST /v1/paymasters/tasks/{taskId}/complete
```

Configuration:

```json
{
  "inboundAction": "task_complete",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "COMPLETE_TASK",
        "operationName": "complete-paymaster-approval-task"
      }
    ]
  }
}
```

Example request body:

```json
{
  "decision": "APPROVE",
  "description": "Approved"
}
```

The REST route extracts `{taskId}` and the normalized operation request includes
`taskId`.

### Approve and execute

Route:

```text
POST /v1/paymasters/processes/{processId}/execute
```

Configuration:

```json
{
  "inboundAction": "approve_and_execute",
  "taskWorkflow": {
    "routingStrategy": "CHAIN_ON_APPROVE",
    "steps": [
      {
        "stepType": "APPROVE_PROCESS",
        "operationName": "approve-paymaster-process-for-execution"
      },
      {
        "stepType": "BUSINESS_OPERATION",
        "operationName": "register-paymaster-in-nab",
        "decisionPolicy": "NAB_PAYMASTER_REGISTRATION"
      },
      {
        "stepType": "BUSINESS_OPERATION",
        "operationName": "grant-paymaster-account-access-in-scm"
      },
      {
        "stepType": "COMPLETE_PROCESS",
        "operationName": "complete-paymaster-process"
      }
    ]
  }
}
```

### Cancel process

Route:

```text
POST /v1/paymasters/processes/{processId}/cancel
```

Configuration:

```json
{
  "inboundAction": "cancel_process",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "CANCEL_PROCESS",
        "operationName": "cancel-paymaster-process"
      }
    ]
  }
}
```

### Find all tasks

Route:

```text
GET /v1/paymasters/tasks
```

Configuration:

```json
{
  "inboundAction": "find_tasks",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "FIND_ALL_TASK",
        "operationName": "find-paymaster-tasks"
      }
    ]
  }
}
```

### Find all processes

Route:

```text
GET /v1/paymasters/processes
```

Configuration:

```json
{
  "inboundAction": "find_processes",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "FIND_ALL_PROCESS",
        "operationName": "find-paymaster-processes"
      }
    ]
  }
}
```

### Find tasks by process id

Route:

```text
GET /v1/paymasters/processes/{processId}/tasks
```

Configuration:

```json
{
  "inboundAction": "find_tasks_by_process_id",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "FIND_TASK_BY_PROCESS_ID",
        "operationName": "find-paymaster-tasks-by-process-id"
      }
    ]
  }
}
```

### Update process description

Route:

```text
PATCH /v1/paymasters/processes/{processId}/description
```

Configuration:

```json
{
  "inboundAction": "update_process_description",
  "taskWorkflow": {
    "routingStrategy": "FIRST",
    "steps": [
      {
        "stepType": "UPDATE_PROCESS_DESCRIPTION",
        "operationName": "update-paymaster-process-description"
      }
    ]
  }
}
```

## URL variables and protocol-neutral input

For REST routes such as:

```text
/v1/paymasters/tasks/{taskId}/complete
```

runtime behavior is:

1. the REST gateway matches the configured path;
2. `{taskId}` is extracted by the REST-specific route layer;
3. it is normalized into SCM inbound parameters;
4. the service layer resolves `taskId` from normalized input;
5. TASK_WORKFLOW does not read URL syntax directly.

The REST binder stores route variables in:

```text
Message.INBOUND_PARAMETERS = Map<String, Object>
```

The same rule applies to non-REST protocols:

```text
ISO
MQ
Kafka
SOAP
TCP
```

Their adapters must normalize identifiers into either:

```text
canonical request payload
or
Message.INBOUND_PARAMETERS
```

The TASK_WORKFLOW service layer must not know whether an identifier originally
came from:

```text
REST path
ISO field
MQ property
Kafka header
SOAP element
```

If the same identifier is supplied in both payload and inbound parameters, equal
values are accepted. Conflicting values fail before provider dispatch.

## Identifier requirements

| `stepType` | Required identifier |
| --- | --- |
| `COMPLETE_TASK` | `taskId` |
| `APPROVE_PROCESS` | `processId` |
| `CANCEL_PROCESS` | `processId` |
| `FIND_TASK_BY_PROCESS_ID` | `processId` |
| `UPDATE_PROCESS_DESCRIPTION` | `processId` |
| `COMPLETE_PROCESS` | current workflow `processId` |

`START_PROCESS`, `FIND_ALL_PROCESS`, and `FIND_ALL_TASK` do not require
`taskId` or `processId` in the routing layer.

Identifier names are matched case-insensitively. `processId` can also be read
from payload field `id`.

Accepted values:

```text
integral JSON numbers
numeric strings
integral Java number values
```

Fail-fast behavior:

| Case | Current error shape |
| --- | --- |
| Missing required id | `TASK_WORKFLOW stepType=<STEP> requires taskId/processId` |
| Blank value | `Invalid TASK_WORKFLOW <id> value from <source> for stepType=<STEP>; expected an integral number or numeric string` |
| Nonnumeric value | Same invalid-value error |
| Fractional value | Same invalid-value error when it cannot be converted exactly to `Long` |
| Conflicting payload and inbound-parameter values | `Conflicting TASK_WORKFLOW <id> values were supplied by normalized payload and inbound parameters` |
| Conflicting payload aliases, such as `id` and `processId` | `Conflicting TASK_WORKFLOW <id> values were supplied by normalized payload fields ...` |
| Invalid inbound parameter map type | `TASK_WORKFLOW exchange property scmInboundParameters must be a Map<String, Object>` |

## Startup validation

Current startup/route-plan validation includes:

```text
TASK_WORKFLOW service must have at least one INBOUND definition
every inbound command requires its own Definition
Definition.details must be valid JSON
Definition.details must be a JSON object
inboundAction is required and must be supported
COMPLETE_PROCESS cannot be exposed as a direct inbound command
taskWorkflow is required
routingStrategy must be FIRST or CHAIN_ON_APPROVE
steps must not be empty
step object must be a JSON object
stepType is required
operationName is required
decisionPolicy must be a nonblank string when supplied
unsupported step fields are rejected
operationName must reference an active ServiceOperation connected to the service
duplicate inbound commands are rejected
duplicate operationName values in one action are rejected
FIRST requires exactly one step
task_complete requires FIRST and COMPLETE_TASK
approve_and_execute requires CHAIN_ON_APPROVE
approve_and_execute requires APPROVE_PROCESS, one or more BUSINESS_OPERATION steps, and COMPLETE_PROCESS
COMPLETE_PROCESS cannot be exposed as a direct inbound action
BUSINESS_OPERATION must not target scm-task
active ServiceOperation.operationName must be nonblank
duplicate active ServiceOperation.operationName values are rejected
operation metadata must be available for every operationName
decisionPolicy must resolve to a registered ChainStepDecisionPolicy
```

Gateway inbound route validation also includes:

```text
path must not be blank
duplicate method + path for the same service and gateway is rejected
duplicate channelServiceDefinitionId for generated route identity is rejected
inboundAction, when present for gateway binding, must be a string and not blank
```

Provider capability validation:

```text
scm-provider-task supports START_PROCESS, APPROVE_PROCESS, COMPLETE_PROCESS,
CANCEL_PROCESS, COMPLETE_TASK, FIND_ALL_TASK, FIND_ALL_PROCESS,
FIND_TASK_BY_PROCESS_ID, and UPDATE_PROCESS_DESCRIPTION.

scm-provider-task does not support BUSINESS_OPERATION.
```

The current route-plan validator rejects `BUSINESS_OPERATION` targeting an
`scm-task:` provider at startup. The provider also checks the typed step
property at runtime and rejects unsupported step types defensively.

## Troubleshooting

| Error | Likely cause | Inspect | Correction |
| --- | --- | --- | --- |
| Route is not created | No active inbound definition or runtime gateway target does not include the gateway | `TBL_SCM_CHN_SVC_DEFINITION`, runtime gateway name, `GatewayChannel.active` | Add an active `INBOUND` definition for the target gateway and start the matching runtime |
| Method or path is missing | Inbound model was not hydrated with REST method/path | linked `Definition.details`, `ChannelServiceDefinitionMapper` behavior | Provide route `method` and `path` through the current mapper-backed route metadata |
| Gateway channel is inactive | `GatewayChannel.active` is false | `REF.TBL_SCM_GATEWAY_CHANNEL` | Activate the gateway channel |
| Channel-service access is inactive | `ChannelServiceAccess.active` is false | `REF.CHANNEL_SERVICE_ACCESS` | Activate the service/channel access |
| Service routing strategy is not `TASK_WORKFLOW` | Service routes through a different handler | `REF.EB_SERVICE.ROUTING_STRATEGY` | Set `ROUTING_STRATEGY = TASK_WORKFLOW` |
| Inbound `Definition` is missing | `TBL_SCM_CHN_SVC_DEFINITION.DEFINITION_ID` is absent or broken | `TBL_SCM_CHN_SVC_DEFINITION`, `TBL_SCM_DEFINITION` | Link one definition per inbound action |
| `Definition.details` is invalid JSON | Details cannot be parsed | `TBL_SCM_DEFINITION.DETAILS` | Store a valid JSON object |
| Legacy role field is still used | Old role-based config was copied forward | `Definition.details` | Remove role-based fields; use `stepType` and `operationName` |
| `stepType` is invalid | Value is not in `TaskWorkflowStepType` | `taskWorkflow.steps[]` | Use a current enum value |
| `operationName` is not connected to the service | Plan names an operation not present as active `ServiceOperation` | `TBL_SCM_SERVICE_OPERATION` | Connect the operation to the same service and mark it active |
| Task provider does not support selected `stepType` | `BUSINESS_OPERATION` or another unsupported step targets `scm-task` | Operation provider URI and step type | Route business steps to business providers; route only supported task/process steps to `scm-task` |
| `taskId` or `processId` is unavailable | Protocol adapter did not normalize the id, or payload omitted it | request payload, `Message.INBOUND_PARAMETERS` | Normalize the id into payload or inbound parameters |
| Duplicate route method/path | Two inbound definitions expose same method and normalized path for the same service/gateway | inbound definitions for the service/gateway | Use unique method/path pairs |
| Duplicate `inboundAction` / command | Two inbound definitions resolve to the same command | `Definition.details.inboundAction` | Keep one inbound definition per supported command |
| `CHAIN_ON_APPROVE` order is invalid | Steps are not approve/business/complete | `taskWorkflow.steps` array order | Put `APPROVE_PROCESS` first, one or more `BUSINESS_OPERATION` middle steps, and `COMPLETE_PROCESS` last |

## Operational definition checklist

Before deployment, verify:

```text
service active/published as required by runtime
routing strategy TASK_WORKFLOW
channel access active
gateway channel active
inbound definition type INBOUND
REST method/path configured where applicable
linked Definition exists
Definition.details valid
inboundAction unique per supported command
stepType valid
operationName connected and active
provider active
identifier normalization configured
decision policy registered
no duplicate method/path
startup validation passed
```
