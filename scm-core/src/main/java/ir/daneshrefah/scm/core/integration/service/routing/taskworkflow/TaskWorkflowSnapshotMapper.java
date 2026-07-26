package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.ExecutionOutcome;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureDetails;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
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

    public RoutingExecutionSnapshot initial(
            String executionId,
            RoutingPlan plan,
            String gatewayServiceVersion,
            Long processId,
            RoutingExecutionContext context,
            Instant now
    ) {
        var identity = requiredIdentity(plan);
        List<RoutingStepSnapshot> steps = plan.steps().stream()
                .map(step -> new RoutingStepSnapshot(
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
        return new RoutingExecutionSnapshot(
                RoutingExecutionSnapshot.CURRENT_SCHEMA_VERSION,
                executionId,
                identity.serviceCode(),
                identity.inboundAction(),
                identity.actionPlanName(),
                gatewayServiceVersion,
                identity.definitionId(),
                identity.planFingerprint(),
                plan.routingStrategy(),
                RoutingExecutionState.RUNNING,
                null,
                processId,
                steps,
                recoveryContext(context),
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public RoutingExecutionSnapshot attempted(
            RoutingExecutionSnapshot snapshot,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            String attemptId,
            Instant now
    ) {
        List<RoutingStepSnapshot> steps = replace(
                snapshot,
                step,
                current -> new RoutingStepSnapshot(
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
                RoutingExecutionState.RUNNING,
                null,
                processId(snapshot, context),
                steps,
                recoveryContext(context),
                snapshot.storedResponse(),
                snapshot.storedFailure(),
                attemptId,
                step.stepId(),
                step.stepIndex(),
                now
        );
    }

    public RoutingExecutionSnapshot decided(
            RoutingExecutionSnapshot snapshot,
            RoutingStepPlan step,
            RoutingStepExecutionResult result,
            RoutingExecutionContext context,
            RoutingExecutionState state,
            RoutingDecision executionDecision,
            StoredRoutingResponse storedResponse,
            RoutingFailureDetails storedFailure,
            Instant now
    ) {
        List<RoutingStepSnapshot> steps = replace(
                snapshot,
                step,
                current -> new RoutingStepSnapshot(
                        current.stepId(),
                        current.stepIndex(),
                        current.stepType(),
                        current.operationName(),
                        result.decision(),
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
                executionDecision,
                processId(snapshot, context),
                steps,
                recoveryContext(context),
                storedResponse,
                storedFailure,
                snapshot.activeAttemptId(),
                snapshot.activeStepId(),
                snapshot.activeStepIndex(),
                now
        );
    }

    public RoutingExecutionSnapshot failed(
            RoutingExecutionSnapshot snapshot,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            RoutingFailureDetails failure,
            Instant now
    ) {
        List<RoutingStepSnapshot> steps = replace(
                snapshot,
                step,
                current -> new RoutingStepSnapshot(
                        current.stepId(),
                        current.stepIndex(),
                        current.stepType(),
                        current.operationName(),
                        RoutingDecision.FAIL,
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
                RoutingExecutionState.FAILED,
                RoutingDecision.FAIL,
                processId(snapshot, context),
                steps,
                recoveryContext(context),
                null,
                failure,
                snapshot.activeAttemptId(),
                snapshot.activeStepId(),
                snapshot.activeStepIndex(),
                now
        );
    }

    public RoutingExecutionSnapshot terminal(
            RoutingExecutionSnapshot snapshot,
            RoutingExecutionContext context,
            RoutingExecutionState state,
            RoutingDecision decision,
            StoredRoutingResponse storedResponse,
            Instant now
    ) {
        return copy(
                snapshot,
                state,
                decision,
                processId(snapshot, context),
                snapshot.steps(),
                recoveryContext(context),
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
            RoutingExecutionSnapshot snapshot
    ) {
        RoutingRecoveryContext recovery = snapshot.minimalRecoveryContext();
        Map<String, Object> stepResults = new LinkedHashMap<>();
        if (recovery != null) {
            recovery.stepResults().forEach((key, value) ->
                    stepResults.put(key, value == null ? null : value.deepCopy()));
        }
        return new RoutingExecutionContext(
                originalRequest,
                snapshot.processId(),
                recovery == null ? null : recovery.correlationId(),
                recovery == null || recovery.transactionData() == null
                        ? null
                        : recovery.transactionData().deepCopy(),
                stepResults
        );
    }

    public StoredRoutingResponse storeResponse(
            Message response
    ) {
        return new StoredRoutingResponse(
                response.getStatus(),
                sanitize(response.getPayload()),
                response.getExecutionOutcome()
        );
    }

    public Message restoreResponse(StoredRoutingResponse response) {
        if (response == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Completed execution snapshot has no stored response");
        }
        return Message.builder()
                .status(response.status())
                .payload(response.payload() == null
                        ? objectMapper.nullNode()
                        : response.payload().deepCopy())
                .executionOutcome(response.executionOutcome())
                .build();
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

    private RoutingRecoveryContext recoveryContext(
            RoutingExecutionContext context
    ) {
        Map<String, JsonNode> results = new LinkedHashMap<>();
        context.stepResults().forEach((stepId, value) ->
                results.put(stepId, sanitize(toJsonNode(value))));
        return new RoutingRecoveryContext(
                context.correlationId(),
                sanitize(toJsonNode(context.transactionData())),
                results
        );
    }

    private Long processId(
            RoutingExecutionSnapshot snapshot,
            RoutingExecutionContext context
    ) {
        return context.processId() == null
                ? snapshot.processId()
                : context.processId();
    }

    private List<RoutingStepSnapshot> replace(
            RoutingExecutionSnapshot snapshot,
            RoutingStepPlan step,
            java.util.function.UnaryOperator<RoutingStepSnapshot> replacement
    ) {
        if (step.stepIndex() >= snapshot.steps().size()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Snapshot does not contain stepIndex=" + step.stepIndex());
        }
        List<RoutingStepSnapshot> copy = new ArrayList<>(snapshot.steps());
        RoutingStepSnapshot current = copy.get(step.stepIndex());
        if (!step.stepId().equals(current.stepId())
                || step.stepIndex() != current.stepIndex()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Snapshot step identity does not match current plan at stepIndex="
                            + step.stepIndex());
        }
        copy.set(step.stepIndex(), replacement.apply(current));
        return List.copyOf(copy);
    }

    private RoutingExecutionSnapshot copy(
            RoutingExecutionSnapshot source,
            RoutingExecutionState state,
            RoutingDecision decision,
            Long processId,
            List<RoutingStepSnapshot> steps,
            RoutingRecoveryContext context,
            StoredRoutingResponse response,
            RoutingFailureDetails failure,
            String activeAttemptId,
            String activeStepId,
            Integer activeStepIndex,
            Instant updatedAt
    ) {
        return new RoutingExecutionSnapshot(
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
                context,
                response,
                failure,
                activeAttemptId,
                activeStepId,
                activeStepIndex,
                source.createdAt(),
                updatedAt
        );
    }

    private ir.daneshrefah.scm.core.integration.service.routing.RoutingPlanIdentity
    requiredIdentity(RoutingPlan plan) {
        if (plan.identity() == null) {
            throw new IllegalStateException(
                    "TASK_WORKFLOW routing plan requires a stable plan identity");
        }
        return plan.identity();
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
