package ir.daneshrefah.scm.core.integration.observability.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.trace.TraceAttribute;

import java.util.List;

public final class CoreTraceAttributes {
    private static final String OWNER = "scm-core";

    public static final ObservationAttributeKey<String> GATEWAY_CHANNEL_CODE = keyword(
            "scm.gateway.channel.code", "Configured gateway channel code.");
    public static final ObservationAttributeKey<String> CLIENT_ID = keyword("scm.client.id", "Client identifier.");
    public static final ObservationAttributeKey<String> CLIENT_TYPE = keyword("scm.client.type", "Client type.");
    public static final ObservationAttributeKey<String> CLIENT_CHANNEL_CODE = keyword(
            "scm.client.channel.code", "Client declared channel code.");
    public static final ObservationAttributeKey<Boolean> CLIENT_CHANNEL_VALIDATED = TraceAttribute.booleanValue(
            "scm.client.channel.validated", OWNER, ObservationAttributePresence.EVENT_OPTIONAL,
            "Whether the client channel was validated for the gateway.");
    public static final ObservationAttributeKey<String> CLIENT_USERNAME = keyword(
            "scm.client.username", "Client supplied username.");
    public static final ObservationAttributeKey<String> CLIENT_ADDRESS = keyword(
            "scm.client.address", "Client network address.");
    public static final ObservationAttributeKey<String> CLIENT_CORRELATION_ID = keyword(
            "scm.client.correlation.id", "Client supplied correlation identifier.");
    public static final ObservationAttributeKey<String> AUTH_TYPE = keyword("scm.auth.type", "Authentication type.");
    public static final ObservationAttributeKey<String> AUTH_SCHEME = keyword("scm.auth.scheme", "Authentication scheme.");
    public static final ObservationAttributeKey<String> AUTH_CLIENT_ID = keyword(
            "scm.auth.client.id", "Authenticated client identifier.");
    public static final ObservationAttributeKey<String> AUTH_CLIENT_ACCEPT_ADDRESS = keyword(
            "scm.auth.client.accept_address", "Authenticated client accepted address.");
    public static final ObservationAttributeKey<String> AUTH_SUBJECT_ID = keyword(
            "scm.auth.subject.id", "Authenticated subject identifier.");
    public static final ObservationAttributeKey<String> AUTH_SUBJECT_USERNAME = keyword(
            "scm.auth.subject.username", "Authenticated subject username.");
    public static final ObservationAttributeKey<String> AUTH_ISSUER = keyword(
            "scm.auth.issuer", "Authentication issuer.");
    public static final ObservationAttributeKey<List<String>> AUTH_AUDIENCE = TraceAttribute.keywordCollection(
            "scm.auth.audience", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Authentication audiences.");
    public static final ObservationAttributeKey<List<String>> AUTH_SCOPES = TraceAttribute.keywordCollection(
            "scm.auth.scopes", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Authentication scopes.");
    public static final ObservationAttributeKey<String> AUTH_LOGIN_METHOD = keyword(
            "scm.auth.login_method", "Authentication login method.");
    public static final ObservationAttributeKey<String> AUTH_TRANSACTION_METHOD = keyword(
            "scm.auth.transaction_method", "Authentication transaction method.");
    public static final ObservationAttributeKey<String> SERVICE_CODE = keyword("scm.service.code", "Service code.");
    public static final ObservationAttributeKey<String> SERVICE_NAME = keyword("scm.service.name", "Service name.");
    public static final ObservationAttributeKey<String> SERVICE_VERSION = keyword("scm.service.version", "Service version.");
    public static final ObservationAttributeKey<Long> SERVICE_AMOUNT = TraceAttribute.longNumber(
            "scm.service.amount", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Business transaction amount.");
    public static final ObservationAttributeKey<String> SERVICE_CURRENCY = keyword(
            "scm.service.currency", "Business transaction currency.");
    public static final ObservationAttributeKey<String> SERVICE_SOURCE_CARD = keyword(
            "scm.service.source.card", "Business source card number.");
    public static final ObservationAttributeKey<String> SERVICE_SOURCE_ACCOUNT = keyword(
            "scm.service.source.account", "Business source account number.");
    public static final ObservationAttributeKey<String> SERVICE_DESTINATION_CARD = keyword(
            "scm.service.destination.card", "Business destination card number.");
    public static final ObservationAttributeKey<String> SERVICE_DESTINATION_ACCOUNT = keyword(
            "scm.service.destination.account", "Business destination account number.");
    public static final ObservationAttributeKey<String> SERVICE_DOCUMENT_NUMBER = keyword(
            "scm.service.document.number", "Business document number.");
    public static final ObservationAttributeKey<String> SERVICE_REFERENCE_NUMBER = keyword(
            "scm.service.reference.number", "Business reference number.");
    public static final ObservationAttributeKey<String> SERVICE_TRANSACTION_TYPE = keyword(
            "scm.service.transaction.type", "Business transaction type.");
    public static final ObservationAttributeKey<String> STATUS_CODE = keyword("scm.status.code", "Business status code.");
    public static final ObservationAttributeKey<String> STATUS_OUTCOME = keyword(
            "scm.status.outcome", "Business status outcome.");
    public static final ObservationAttributeKey<String> STATUS_MESSAGE = keyword(
            "scm.status.message", "Business status message.");
    public static final ObservationAttributeKey<String> STATUS_DESCRIPTION = keyword(
            "scm.status.description", "Business status description.");
    public static final ObservationAttributeKey<String> OPERATION_CODE = keyword("scm.operation.code", "Operation code.");
    public static final ObservationAttributeKey<String> OPERATION_NAME = keyword("scm.operation.name", "Operation name.");
    public static final ObservationAttributeKey<String> OPERATION_TYPE = keyword("scm.operation.type", "Operation type.");
    public static final ObservationAttributeKey<String> ROUTING_STRATEGY = keyword(
            "scm.routing.strategy", "Routing engine used to execute the operation.");
    public static final ObservationAttributeKey<Long> ROUTING_STEP_INDEX = TraceAttribute.longNumber(
            "scm.routing.step.index", OWNER, ObservationAttributePresence.EVENT_OPTIONAL,
            "Zero-based routing step index.");
    public static final ObservationAttributeKey<String> ROUTING_STEP_ID = keyword(
            "scm.routing.step.id", "Stable routing step identifier.");
    public static final ObservationAttributeKey<String> ROUTING_EXECUTION_ID = keyword(
            "scm.routing.execution.id", "Task workflow execution identifier.");
    public static final ObservationAttributeKey<String> TASK_INBOUND_ACTION = keyword(
            "scm.task.inbound_action", "Task workflow inbound action.");
    public static final ObservationAttributeKey<String> TASK_ACTION_PLAN_NAME = keyword(
            "scm.task.action_plan_name", "Selected internal ActionPlan name.");
    public static final ObservationAttributeKey<String> TASK_WORKFLOW_STEP_TYPE = keyword(
            "scm.task.step_type", "Task workflow step type.");
    public static final ObservationAttributeKey<String> ROUTING_DECISION = keyword(
            "scm.routing.decision", "Decision returned by the routing policy.");
    public static final ObservationAttributeKey<Boolean> ROUTING_RETRYABLE =
            TraceAttribute.booleanValue(
                    "scm.routing.retryable",
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    "Whether the routing outcome can be retried.");
    public static final ObservationAttributeKey<String> OPERATION_NORMALIZED_OUTCOME = keyword(
            "scm.operation.normalized_outcome", "Normalized operation outcome used by routing.");
    public static final ObservationAttributeKey<Long> OPERATION_DURATION_MS = TraceAttribute.longNumber(
            "scm.operation.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Operation duration in milliseconds.");
    public static final ObservationAttributeKey<Long> SERVICE_DURATION_MS = TraceAttribute.longNumber(
            "scm.service.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Service duration in milliseconds.");
    public static final ObservationAttributeKey<String> EXCHANGE_ID = keyword("scm.exchange.id", "Camel exchange identifier.");
    public static final ObservationAttributeKey<String> TARGET_KIND = keyword("scm.runtime.target.kind", "Runtime target kind.");
    public static final ObservationAttributeKey<String> PLUGIN_NAME = keyword("plugin.name", "Plugin name.");
    public static final ObservationAttributeKey<String> PLUGIN_TYPE = keyword("plugin.type", "Plugin type.");
    public static final ObservationAttributeKey<String> PLUGIN_PHASE = keyword("plugin.phase", "Plugin phase.");
    public static final ObservationAttributeKey<String> PLUGIN_LAYER = keyword("plugin.layer", "Plugin layer.");
    public static final ObservationAttributeKey<Long> PLUGIN_DURATION_MS = TraceAttribute.longNumber(
            "plugin.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Plugin duration in milliseconds.");

    private CoreTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                GATEWAY_CHANNEL_CODE,
                CLIENT_ID, CLIENT_TYPE, CLIENT_CHANNEL_CODE, CLIENT_CHANNEL_VALIDATED, CLIENT_USERNAME,
                CLIENT_ADDRESS, CLIENT_CORRELATION_ID,
                AUTH_TYPE, AUTH_SCHEME, AUTH_CLIENT_ID, AUTH_CLIENT_ACCEPT_ADDRESS,
                AUTH_SUBJECT_ID, AUTH_SUBJECT_USERNAME, AUTH_ISSUER, AUTH_AUDIENCE, AUTH_SCOPES,
                AUTH_LOGIN_METHOD, AUTH_TRANSACTION_METHOD,
                SERVICE_CODE, SERVICE_NAME, SERVICE_VERSION,
                SERVICE_AMOUNT, SERVICE_CURRENCY,
                SERVICE_SOURCE_CARD, SERVICE_SOURCE_ACCOUNT,
                SERVICE_DESTINATION_CARD, SERVICE_DESTINATION_ACCOUNT,
                SERVICE_DOCUMENT_NUMBER, SERVICE_REFERENCE_NUMBER, SERVICE_TRANSACTION_TYPE,
                STATUS_CODE, STATUS_OUTCOME, STATUS_MESSAGE, STATUS_DESCRIPTION,
                OPERATION_CODE, OPERATION_NAME, OPERATION_TYPE,
                ROUTING_STRATEGY, ROUTING_STEP_ID, ROUTING_STEP_INDEX,
                ROUTING_EXECUTION_ID, TASK_INBOUND_ACTION,
                TASK_ACTION_PLAN_NAME, TASK_WORKFLOW_STEP_TYPE,
                ROUTING_DECISION, ROUTING_RETRYABLE, OPERATION_NORMALIZED_OUTCOME,
                OPERATION_DURATION_MS, SERVICE_DURATION_MS,
                EXCHANGE_ID, TARGET_KIND,
                PLUGIN_NAME, PLUGIN_TYPE, PLUGIN_PHASE, PLUGIN_LAYER, PLUGIN_DURATION_MS
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
