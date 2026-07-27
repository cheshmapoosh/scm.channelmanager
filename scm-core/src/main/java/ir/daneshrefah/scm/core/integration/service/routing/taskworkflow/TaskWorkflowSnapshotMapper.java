package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.ExecutionOutcome;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Boundary between core routing state and the provider-owned versioned
 * persistence model.
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
        List<TaskWorkflowExecutionSnapshot.StepState> steps =
                plan.steps().stream()
                        .map(step -> new TaskWorkflowExecutionSnapshot.StepState(
                                step.stepId(),
                                step.stepIndex(),
                                null,
                                0,
                                null,
                                null,
                                null,
                                null
                        ))
                        .toList();
        return new TaskWorkflowExecutionSnapshot(
                TaskWorkflowExecutionSnapshot.CURRENT_SCHEMA_VERSION,
                new TaskWorkflowExecutionSnapshot.ExecutionIdentity(
                        executionId,
                        processId,
                        gatewayServiceVersion
                ),
                new TaskWorkflowExecutionSnapshot.PlanIdentity(
                        identity.serviceCode(),
                        identity.inboundAction(),
                        identity.actionPlanName(),
                        identity.definitionId(),
                        identity.planFingerprint()
                ),
                runningStatus(null, null),
                steps,
                resumeData(context, null, null, null),
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
            Instant now
    ) {
        List<TaskWorkflowExecutionSnapshot.StepState> steps = replace(
                snapshot,
                step,
                current -> new TaskWorkflowExecutionSnapshot.StepState(
                        current.stepId(),
                        current.stepIndex(),
                        current.decision(),
                        current.attemptCount() + 1,
                        current.normalizedOutcome(),
                        current.reasonCode(),
                        current.messageStatus(),
                        now
                )
        );
        return copy(
                snapshot,
                processId(snapshot, context),
                runningStatus(attemptId, step.stepIndex()),
                steps,
                resumeData(context, snapshot.resume(), null, null),
                null,
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
            TaskWorkflowExecutionSnapshot.StoredFailure storedFailure,
            Object retryRequest,
            Instant now
    ) {
        List<TaskWorkflowExecutionSnapshot.StepState> steps = replace(
                snapshot,
                step,
                current -> new TaskWorkflowExecutionSnapshot.StepState(
                        current.stepId(),
                        current.stepIndex(),
                        storedDecision(result.decision()),
                        current.attemptCount(),
                        result.decisionResult().normalizedOutcome(),
                        result.decisionResult().reasonCode(),
                        result.decisionResult().messageStatus(),
                        current.attemptedAt()
                )
        );
        TaskWorkflowExecutionSnapshot.TerminalOutcome terminal =
                storedFailure == null
                        ? null
                        : new TaskWorkflowExecutionSnapshot.TerminalOutcome(
                                null,
                                storedFailure
                        );
        return copy(
                snapshot,
                processId(snapshot, context),
                new TaskWorkflowExecutionSnapshot.ExecutionStatus(
                        state,
                        storedDecision(executionDecision),
                        snapshot.status().activeAttemptId(),
                        snapshot.status().activeStepIndex()
                ),
                steps,
                state == TaskWorkflowExecutionState.FAILED
                        ? null
                        : resumeData(
                                context,
                                snapshot.resume(),
                                step,
                                result,
                                retryRequest
                        ),
                terminal,
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
        List<TaskWorkflowExecutionSnapshot.StepState> steps = replace(
                snapshot,
                step,
                current -> new TaskWorkflowExecutionSnapshot.StepState(
                        current.stepId(),
                        current.stepIndex(),
                        TaskWorkflowExecutionDecision.FAIL,
                        Math.max(1, current.attemptCount()),
                        failure.normalizedOutcome(),
                        failure.reasonCode(),
                        failure.messageStatus(),
                        current.attemptedAt() == null
                                ? now
                                : current.attemptedAt()
                )
        );
        return copy(
                snapshot,
                processId(snapshot, context),
                new TaskWorkflowExecutionSnapshot.ExecutionStatus(
                        TaskWorkflowExecutionState.FAILED,
                        TaskWorkflowExecutionDecision.FAIL,
                        snapshot.status().activeAttemptId(),
                        snapshot.status().activeStepIndex()
                ),
                steps,
                null,
                new TaskWorkflowExecutionSnapshot.TerminalOutcome(
                        null,
                        storeFailure(failure)
                ),
                now
        );
    }

    public TaskWorkflowExecutionSnapshot terminal(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingExecutionContext context,
            TaskWorkflowExecutionState state,
            RoutingDecision decision,
            TaskWorkflowExecutionSnapshot.StoredResponse storedResponse,
            Instant now
    ) {
        return copy(
                snapshot,
                processId(snapshot, context),
                new TaskWorkflowExecutionSnapshot.ExecutionStatus(
                        state,
                        storedDecision(decision),
                        snapshot.status().activeAttemptId(),
                        snapshot.status().activeStepIndex()
                ),
                snapshot.steps(),
                decision == RoutingDecision.RETRY_LATER
                        ? snapshot.resume()
                        : null,
                new TaskWorkflowExecutionSnapshot.TerminalOutcome(
                        storedResponse,
                        null
                ),
                now
        );
    }

    public RoutingExecutionContext restoreContext(
            Object originalRequest,
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan,
            String processCorrelationId
    ) {
        TaskWorkflowExecutionSnapshot.ResumeData resume = snapshot.resume();
        Object lastBusinessResponse = resume == null
                ? null
                : copy(resume.lastBusinessResponse());
        Map<String, Object> stepResults = new LinkedHashMap<>();
        String lastBusinessStepId = lastSuccessfulBusinessStepId(
                snapshot,
                plan
        );
        if (lastBusinessStepId != null && lastBusinessResponse != null) {
            stepResults.put(lastBusinessStepId, lastBusinessResponse);
        }
        return new RoutingExecutionContext(
                originalRequest,
                snapshot.execution().processId(),
                processCorrelationId,
                resume == null ? null : copy(resume.transactionData()),
                lastBusinessResponse,
                retryStepId(snapshot),
                resume == null ? null : copy(resume.retryRequest()),
                stepResults
        );
    }

    public TaskWorkflowExecutionSnapshot.StoredResponse storeResponse(
            Message response
    ) {
        return new TaskWorkflowExecutionSnapshot.StoredResponse(
                response.getStatus(),
                sanitize(response.getPayload())
        );
    }

    public Message restoreResponse(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan
    ) {
        TaskWorkflowExecutionSnapshot.StoredResponse response =
                snapshot.terminal() == null
                        ? null
                        : snapshot.terminal().response();
        if (response == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Completed execution snapshot has no stored response"
            );
        }
        JsonNode payload = response.payload() == null
                ? objectMapper.nullNode()
                : response.payload().deepCopy();
        enrichStartExecutionId(payload, snapshot, plan);
        return Message.builder()
                .status(response.status())
                .payload(payload)
                .executionOutcome(executionOutcome(
                        snapshot,
                        plan,
                        responseReasonCode(snapshot)
                ))
                .build();
    }

    public RoutingFailureDetails restoreFailure(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan
    ) {
        TaskWorkflowExecutionSnapshot.StoredFailure failure =
                snapshot.terminal() == null
                        ? null
                        : snapshot.terminal().failure();
        if (failure == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Failed execution snapshot has no stored failure"
            );
        }
        int stepIndex = failure.stepIndex() == null
                ? -1
                : failure.stepIndex();
        if (stepIndex < 0 || stepIndex >= plan.steps().size()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Stored failure stepIndex does not match the current plan"
            );
        }
        RoutingStepPlan step = plan.steps().get(stepIndex);
        return new RoutingFailureDetails(
                plan.identity(),
                plan.planId(),
                step.stepId(),
                stepIndex,
                step.serviceOperation().getOperationName(),
                RoutingDecision.FAIL,
                failure.messageStatus(),
                failure.reasonCode(),
                failure.reasonMessage(),
                failure.normalizedOutcome()
        );
    }

    public ExecutionOutcome executionOutcome(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan,
            String reasonCode
    ) {
        TaskWorkflowExecutionDecision decision = snapshot.status().decision();
        String executionId = snapshot.execution().processId() == null
                ? null
                : snapshot.execution().executionId();
        return new ExecutionOutcome(
                executionId,
                snapshot.plan().serviceCode(),
                snapshot.plan().inboundAction(),
                snapshot.plan().actionPlanName(),
                snapshot.execution().gatewayServiceVersion(),
                plan.routingStrategy().name(),
                decision == null ? null : decision.name(),
                decision == TaskWorkflowExecutionDecision.RETRY_LATER,
                reasonCode
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

    public TaskWorkflowExecutionSnapshot.StoredFailure storeFailure(
            RoutingFailureDetails failure
    ) {
        return new TaskWorkflowExecutionSnapshot.StoredFailure(
                failure.stepIndex(),
                failure.messageStatus(),
                failure.reasonCode(),
                failure.reasonMessage(),
                failure.normalizedOutcome()
        );
    }

    private TaskWorkflowExecutionSnapshot.ResumeData resumeData(
            RoutingExecutionContext context,
            TaskWorkflowExecutionSnapshot.ResumeData current,
            RoutingStepPlan step,
            RoutingStepExecutionResult result
    ) {
        return resumeData(context, current, step, result, null);
    }

    private TaskWorkflowExecutionSnapshot.ResumeData resumeData(
            RoutingExecutionContext context,
            TaskWorkflowExecutionSnapshot.ResumeData current,
            RoutingStepPlan step,
            RoutingStepExecutionResult result,
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
        if (transactionData == null
                && storedRetryRequest == null
                && lastBusinessResponse == null) {
            return null;
        }
        return new TaskWorkflowExecutionSnapshot.ResumeData(
                transactionData,
                storedRetryRequest,
                lastBusinessResponse
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
                ? snapshot.execution().processId()
                : context.processId();
    }

    private List<TaskWorkflowExecutionSnapshot.StepState> replace(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingStepPlan step,
            java.util.function.UnaryOperator<
                    TaskWorkflowExecutionSnapshot.StepState> replacement
    ) {
        if (step.stepIndex() >= snapshot.steps().size()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Snapshot does not contain stepIndex=" + step.stepIndex()
            );
        }
        List<TaskWorkflowExecutionSnapshot.StepState> copy =
                new ArrayList<>(snapshot.steps());
        TaskWorkflowExecutionSnapshot.StepState current =
                copy.get(step.stepIndex());
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
            Long processId,
            TaskWorkflowExecutionSnapshot.ExecutionStatus status,
            List<TaskWorkflowExecutionSnapshot.StepState> steps,
            TaskWorkflowExecutionSnapshot.ResumeData resume,
            TaskWorkflowExecutionSnapshot.TerminalOutcome terminal,
            Instant updatedAt
    ) {
        return new TaskWorkflowExecutionSnapshot(
                source.schemaVersion(),
                new TaskWorkflowExecutionSnapshot.ExecutionIdentity(
                        source.execution().executionId(),
                        processId,
                        source.execution().gatewayServiceVersion()
                ),
                source.plan(),
                status,
                steps,
                resume,
                terminal,
                source.createdAt(),
                updatedAt
        );
    }

    private TaskWorkflowExecutionSnapshot.ExecutionStatus runningStatus(
            String activeAttemptId,
            Integer activeStepIndex
    ) {
        return new TaskWorkflowExecutionSnapshot.ExecutionStatus(
                TaskWorkflowExecutionState.RUNNING,
                null,
                activeAttemptId,
                activeStepIndex
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
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan
    ) {
        String selected = null;
        for (int index = 0; index < snapshot.steps().size(); index++) {
            TaskWorkflowExecutionSnapshot.StepState state =
                    snapshot.steps().get(index);
            RoutingStepPlan step = plan.steps().get(index);
            if (step.observationContext().taskWorkflowStepType()
                    == TaskWorkflowStepType.BUSINESS_OPERATION
                    && state.decision()
                    == TaskWorkflowExecutionDecision.SUCCESS) {
                selected = state.stepId();
            }
        }
        return selected;
    }

    private String retryStepId(TaskWorkflowExecutionSnapshot snapshot) {
        return snapshot.steps().stream()
                .filter(step -> step.decision()
                        == TaskWorkflowExecutionDecision.RETRY_LATER)
                .map(TaskWorkflowExecutionSnapshot.StepState::stepId)
                .findFirst()
                .orElse(null);
    }

    private String responseReasonCode(
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        Integer activeStep = snapshot.status().activeStepIndex();
        if (activeStep != null && activeStep < snapshot.steps().size()) {
            return snapshot.steps().get(activeStep).reasonCode();
        }
        for (int index = snapshot.steps().size() - 1; index >= 0; index--) {
            String reasonCode = snapshot.steps().get(index).reasonCode();
            if (reasonCode != null) {
                return reasonCode;
            }
        }
        return null;
    }

    private RoutingPlanIdentity requiredIdentity(RoutingPlan plan) {
        if (plan.identity() == null) {
            throw new IllegalStateException(
                    "TASK_WORKFLOW routing plan requires a stable plan identity"
            );
        }
        return plan.identity();
    }

    private void enrichStartExecutionId(
            JsonNode payload,
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan
    ) {
        if (!(payload instanceof ObjectNode objectPayload)
                || snapshot.execution().processId() == null
                || plan.steps().size() != 1
                || plan.steps().getFirst()
                .observationContext()
                .taskWorkflowStepType()
                != TaskWorkflowStepType.START_PROCESS) {
            return;
        }
        objectPayload.put(
                "executionId",
                snapshot.execution().executionId()
        );
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
