package ir.daneshrefah.scm.logging.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LogAttribute {
    TERMINAL_CODE("terminalCode"),
    CHANNEL_CODE("channelCode"),
    CLIENT_ID("clientId"),
    CORRELATION_ID("correlationId"),
    CLIENT_CORRELATION_ID("clientCorrelationId"),
    FLOW_ID("flowId"),
    CLIENT_FLOW_ID("clientFlowId"),
    SERVICE_CODE("serviceCode"),
    USERNAME("username"),
    NICKNAME("nickname"),
    DELEGATOR_USERNAME("delegatorUsername"),
    DELEGATOR_NICKNAME("delegatorNickname"),
    MESSAGE_ID("messageId"),
    PARENT_MESSAGE_ID("parentMessageId"),
    PROVIDER_CODE("providerCode"),
    PROVIDER_RESPONSE_CODE("providerResponseCode"),
    PROVIDER_TARGET_URL("providerTargetUrl"),
    PROVIDER_PROTOCOL("providerProtocol"),
    REQUEST_BODY_TYPE("requestBodyType"),
    REQUEST_HEADERS("requestHeaders"),
    RESPONSE_BODY_TYPE("responseBodyType"),
    RESPONSE_HEADERS("responseHeaders"),
    THREAD_NAME("threadName"),
    CHANNEL_CLASS_NAME("channelClassName"),
    MESSAGE_STATUS("messageStatus"),
    HOST_ADDRESS("hostAddress"),
    ERRORS("errors"),
    RESPONSE("response"),
    REQUEST("request"),
    EXCEPTION_CLASS_NAME("exceptionClassName"),
    EVENT_TYPE("eventType"),
    RESPONSE_STATUS_CODE("responseStatusCode"),
    VERSION("version"),
    END_POINT("endpoint"),
    METHOD_TYPE("methodType"),
    END_TIME("endTime"),
    AMOUNT("amount"),
    ACCOUNT_NO("accountNo"),
    CARD_NO("cardNo");
    private final String attributeName;
}
