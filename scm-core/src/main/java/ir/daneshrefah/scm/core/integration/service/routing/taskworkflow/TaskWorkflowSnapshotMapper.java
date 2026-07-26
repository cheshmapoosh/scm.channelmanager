package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureDetails;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlanIdentity;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionDecision;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionSnapshot;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionState;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowResumeData;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowStepSnapshot;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowStoredFailure;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowStoredResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The only mapper between core routing state and provider-owned persistence
 * DTOs. Generic operation responses remain in memory and are never copied into
 * the durable snapshot.
 */
public class TaskWorkflowSnapshotMapper {

    private static final Set<String> SENSITIVE_FIELD_PARTS = Set.of(
            "authorization",
            "authentication",
            "password",
            "passwd",
            "secret",
            "token",
            "cookie",
            "pin",
            "cvv"
    );

    private final ObjectMapper objectMapper;

    public TaskWorkflowSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TaskWorkflowExecutionSnapshot initial(
            String executionId,
            RoutingPlan plan,
            String gatewayServiceVersion,
            Long processId,
            RoutingExecutionContext context,
            Instant now
    ) {
        RoutingPlanIdentity identity = requiredIdentity(plan);
        List<TaskWorkflowStepSnapshot> steps = plan.steps().stream()
                .map(step -> new TaskWorkflowStepSnapshot(
                        step.stepId(),
                        step.stepIndex(),
                        step.observationContext().taskWorkflowStepType(),
                        step.serviceOperation().getOperationName(),
                        null,
                        0,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
                .toList();
        return new TaskWorkflowExecutionSnapshot(
                TaskWorkflowExecutionSnapshot.CURRENT_SCHEMA_VERSION,
                executionId,
                identity.serviceCode(),
                identity.inboundAction(),
                identity.actionPlanName(),
                gatewayServiceVersion,
                identity.definitionId(),
                identity.planFingerprint(),
                plan.routingStrategy().name(),
                TaskWorkflowExecutionState.RUNNING,
                null,
                processId,
                steps,
                resumeData(context, null, null, null, null),
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public TaskWorkflowExecutionSnapshot attempted(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            String attemptId,
            String providerIdempotencyKey,
            Instant now
    ) {
        List<TaskWorkflowStepSnapshot> steps = replace(
                snapshot,
                step,
                current -> new TaskWorkflowStepSnapshot(
                        current.stepId(),
                        current.stepIndex(),
                        current.stepType(),
                        current.operationName(),
                        current.decision(),
                        current.attemptCount() + 1,
                        attemptId,
                        current.normalizedOutcome(),
                        current.reasonCode(),
                        current.reasonMessage(),
                        current.messageStatus(),
                        now,
                        now
                )
        );
        return copy(
                snapshot,
                TaskWorkflowExecutionState.RUNNING,
                null,
                processId(snapshot, context),
                steps,
                resumeData(
                        context,
                        snapshot.resumeData(),
                        null,
                        null,
                        providerIdempotencyKey
                ),
                snapshot.storedResponse(),
                snapshot.storedFailure(),
                attemptId,
                step.stepId(),
                step.stepIndex(),
                now
        );
    }

    public TaskWorkflowExecutionSnapshot decided(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingStepPlan step,
            RoutingStepExecutionResult result,
            RoutingExecutionContext context,
            TaskWorkflowExecutionState state,
            RoutingDecision executionDecision,
            TaskWorkflowStoredResponse storedResponse,
            TaskWorkflowStoredFailure storedFailure,
            Object retryRequest,
            String providerIdempotencyKey,
            Instant now
    ) {
        List<TaskWorkflowStepSnapshot> steps = replace(
                snapshot,
                step,
                current -> new TaskWorkflowStepSnapshot(
                        current.stepId(),
                        current.stepIndex(),
                        current.stepType(),
                        current.operationName(),
                        storedDecision(result.decision()),
                        current.attemptCount(),
                        current.attemptId(),
                        result.decisionResult().normalizedOutcome(),
                        result.decisionResult().reasonCode(),
                        result.decisionResult().reasonMessage(),
                        result.decisionResult().messageStatus(),
                        current.attemptedAt(),
                        now
                )
        );
        return copy(
                snapshot,
                state,
                storedDecision(executionDecision),
                processId(snapshot, context),
                steps,
                resumeData(
                        context,
                        snapshot.resumeData(),
                        step,
                        result,
                        providerIdempotencyKey,
                        retryRequest
                ),
                storedResponse,
                storedFailure,
                snapshot.activeAttemptId(),
                snapshot.activeStepId(),
                snapshot.activeStepIndex(),
                now
        );
    }

    public TaskWorkflowExecutionSnapshot failed(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            RoutingFailureDetails failure,
            Instant now
    ) {
        List<TaskWorkflowStepSnapshot> steps = replace(
                snapshot,
                step,
                current -> new TaskWorkflowStepSnapshot(
                        current.stepId(),
                        current.stepIndex(),
                        current.stepType(),
                        current.operationName(),
                        TaskWorkflowExecutionDecision.FAIL,
                        Math.max(1, current.attemptCount()),
                        current.attemptId(),
                        failure.normalizedOutcome(),
                        failure.reasonCode(),
                        failure.reasonMessage(),
                        failure.messageStatus(),
                        current.attemptedAt(),
                        now
                )
        );
        return copy(
                snapshot,
                TaskWorkflowExecutionState.FAILED,
                TaskWorkflowExecutionDecision.FAIL,
                processId(snapshot, context),
                steps,
                null,
                null,
                storeFailure(failure),
                snapshot.activeAttemptId(),
                snapshot.activeStepId(),
                snapshot.activeStepIndex(),
                now
        );
    }

    public TaskWorkflowExecutionSnapshot terminal(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingExecutionContext context,
            TaskWorkflowExecutionState state,
            RoutingDecision decision,
            TaskWorkflowStoredResponse storedResponse,
            Instant now
    ) {
        return copy(
                snapshot,
                state,
                storedDecision(decision),
                processId(snapshot, context),
                snapshot.steps(),
                decision == RoutingDecision.RETRY_LATER
                        ? snapshot.resumeData()
                        : null,
                storedResponse,
                snapshot.storedFailure(),
                snapshot.activeAttemptId(),
                snapshot.activeStepId(),
                snapshot.activeStepIndex(),
                now
        );
    }

    public RoutingExecutionContext restoreContext(
            Object originalRequest,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        TaskWorkflowResumeData resume = snapshot.resumeData();
        Object lastBusinessResponse = resume == null
                ? null
                : copy(resume.lastBusinessResponse());
        Map<String, Object> stepResults = new LinkedHashMap<>();
        String lastBusinessStepId = lastSuccessfulBusinessStepId(snapshot);
        if (lastBusinessStepId != null && lastBusinessResponse != null) {
            stepResults.put(lastBusinessStepId, lastBusinessResponse);
        }
        String retryStepId = retryStepId(snapshot);
        return new RoutingExecutionContext(
                originalRequest,
                snapshot.processId(),
                resume == null ? null : resume.correlationId(),
                resume == null ? null : copy(resume.transactionData()),
                lastBusinessResponse,
                retryStepId,
                resume == null ? null : copy(resume.retryRequest()),
                stepResults
        );
    }

    public TaskWorkflowStoredResponse storeResponse(Message response) {
        return new TaskWorkflowStoredResponse(
                response.getStatus(),
                sanitize(response.getPayload()),
                response.getExecutionOutcome()
        );
    }

    public Message restoreResponse(TaskWorkflowStoredResponse response) {
        if (response == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Completed execution snapshot has no stored response"
            );
        }
        return Message.builder()
                .status(response.status())
                .payload(response.payload() == null
                        ? objectMapper.nullNode()
                        : response.payload().deepCopy())
                .executionOutcome(response.executionOutcome())
                .build();
    }

    public RoutingFailureDetails restoreFailure(
            TaskWorkflowStoredFailure failure
    ) {
        if (failure == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Failed execution snapshot has no stored failure"
            );
        }
        return new RoutingFailureDetails(
                new RoutingPlanIdentity(
                        failure.serviceCode(),
                        failure.inboundAction(),
                        failure.actionPlanName(),
                        failure.definitionId(),
                        failure.planFingerprint()
                ),
                failure.planId(),
                failure.stepId(),
                failure.stepIndex(),
                failure.operationName(),
                RoutingDecision.FAIL,
                failure.messageStatus(),
                failure.reasonCode(),
                failure.reasonMessage(),
                failure.normalizedOutcome()
        );
    }

    public JsonNode toJsonNode(Object value) {
        if (value instanceof Message message) {
            return message.getPayload() == null
                    ? objectMapper.nullNode()
                    : message.getPayload().deepCopy();
        }
        if (value instanceof JsonNode node) {
            return node.deepCopy();
        }
        return value == null
                ? objectMapper.nullNode()
                : objectMapper.valueToTree(value);
    }

    public JsonNode sanitize(JsonNode value) {
        if (value == null) {
            return null;
        }
        if (value.isObject()) {
            ObjectNode sanitized = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = value.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (!sensitive(field.getKey())) {
                    sanitized.set(field.getKey(), sanitize(field.getValue()));
                }
            }
            return sanitized;
        }
        if (value.isArray()) {
            ArrayNode sanitized = objectMapper.createArrayNode();
            value.forEach(item -> sanitized.add(sanitize(item)));
            return sanitized;
        }
        return value.deepCopy();
    }

    private TaskWorkflowResumeData resumeData(
            RoutingExecutionContext context,
            TaskWorkflowResumeData current,
            RoutingStepPlan step,
            RoutingStepExecutionResult result,
            String providerIdempotencyKey
    ) {
        return resumeData(
                context,
                current,
                step,
                result,
                providerIdempotencyKey,
                null
        );
    }

    private TaskWorkflowResumeData resumeData(
            RoutingExecutionContext context,
            TaskWorkflowResumeData current,
            RoutingStepPlan step,
            RoutingStepExecutionResult result,
            String providerIdempotencyKey,
            Object retryRequest
    ) {
        JsonNode transactionData = sanitize(toNullableJsonNode(
                context.transactionData()
        ));
        JsonNode lastBusinessResponse = current == null
                ? null
                : current.lastBusinessResponse();
        if (step != null
                && result != null
                && result.decision() == RoutingDecision.SUCCESS
                && step.observationContext().taskWorkflowStepType()
                == TaskWorkflowStepType.BUSINESS_OPERATION) {
            lastBusinessResponse = sanitize(toJsonNode(result.response()));
        }
        JsonNode storedRetryRequest = null;
        if (step != null
                && result != null
                && result.decision() == RoutingDecision.RETRY_LATER
                && step.observationContext().taskWorkflowStepType()
                != TaskWorkflowStepType.BUSINESS_OPERATION) {
            storedRetryRequest = sanitize(toNullableJsonNode(retryRequest));
        }
        return new TaskWorkflowResumeData(
                context.processId(),
                context.correlationId(),
                transactionData,
                storedRetryRequest,
                lastBusinessResponse,
                providerIdempotencyKey
        );
    }

    private JsonNode toNullableJsonNode(Object value) {
        return value == null ? null : toJsonNode(value);
    }

    private Long processId(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingExecutionContext context
    ) {
        return context.processId() == null
                ? snapshot.processId()
                : context.processId();
    }

    private List<TaskWorkflowStepSnapshot> replace(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingStepPlan step,
            java.util.function.UnaryOperator<TaskWorkflowStepSnapshot> replacement
    ) {
        if (step.stepIndex() >= snapshot.steps().size()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Snapshot does not contain stepIndex=" + step.stepIndex()
            );
        }
        List<TaskWorkflowStepSnapshot> copy =
                new ArrayList<>(snapshot.steps());
        TaskWorkflowStepSnapshot current = copy.get(step.stepIndex());
        if (!step.stepId().equals(current.stepId())
                || step.stepIndex() != current.stepIndex()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Snapshot step identity does not match current plan at "
                            + "stepIndex=" + step.stepIndex()
            );
        }
        copy.set(step.stepIndex(), replacement.apply(current));
        return List.copyOf(copy);
    }

    private TaskWorkflowExecutionSnapshot copy(
            TaskWorkflowExecutionSnapshot source,
            TaskWorkflowExecutionState state,
            TaskWorkflowExecutionDecision decision,
            Long processId,
            List<TaskWorkflowStepSnapshot> steps,
            TaskWorkflowResumeData resumeData,
            TaskWorkflowStoredResponse response,
            TaskWorkflowStoredFailure failure,
            String activeAttemptId,
            String activeStepId,
            Integer activeStepIndex,
            Instant updatedAt
    ) {
        return new TaskWorkflowExecutionSnapshot(
                source.schemaVersion(),
                source.executionId(),
                source.serviceCode(),
                source.inboundAction(),
                source.actionPlanName(),
                source.gatewayServiceVersion(),
                source.definitionId(),
                source.planFingerprint(),
                source.routingStrategy(),
                state,
                decision,
                processId,
                steps,
                resumeData,
                response,
                failure,
                activeAttemptId,
                activeStepId,
                activeStepIndex,
                source.createdAt(),
                updatedAt
        );
    }

    public TaskWorkflowStoredFailure storeFailure(
            RoutingFailureDetails failure
    ) {
        RoutingPlanIdentity identity = failure.planIdentity();
        return new TaskWorkflowStoredFailure(
                identity.serviceCode(),
                identity.inboundAction(),
                identity.actionPlanName(),
                identity.definitionId(),
                identity.planFingerprint(),
                failure.planId(),
                failure.stepId(),
                failure.stepIndex(),
                failure.operationName(),
                failure.messageStatus(),
                failure.reasonCode(),
                failure.reasonMessage(),
                failure.normalizedOutcome()
        );
    }

    private TaskWorkflowExecutionDecision storedDecision(
            RoutingDecision decision
    ) {
        return decision == null
                ? null
                : TaskWorkflowExecutionDecision.valueOf(decision.name());
    }

    private String lastSuccessfulBusinessStepId(
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        String selected = null;
        for (TaskWorkflowStepSnapshot step : snapshot.steps()) {
            if (step.stepType() == TaskWorkflowStepType.BUSINESS_OPERATION
                    && step.decision()
                    == TaskWorkflowExecutionDecision.SUCCESS) {
                selected = step.stepId();
            }
        }
        return selected;
    }

    private String retryStepId(TaskWorkflowExecutionSnapshot snapshot) {
        return snapshot.steps().stream()
                .filter(step -> step.decision()
                        == TaskWorkflowExecutionDecision.RETRY_LATER)
                .map(TaskWorkflowStepSnapshot::stepId)
                .findFirst()
                .orElse(null);
    }

    private RoutingPlanIdentity requiredIdentity(RoutingPlan plan) {
        if (plan.identity() == null) {
            throw new IllegalStateException(
                    "TASK_WORKFLOW routing plan requires a stable plan identity"
            );
        }
        return plan.identity();
    }

    private Object copy(JsonNode value) {
        return value == null ? null : value.deepCopy();
    }

    private boolean sensitive(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "");
        return SENSITIVE_FIELD_PARTS.stream()
                .map(part -> part.replace("_", ""))
                .anyMatch(normalized::contains);
    }
}
