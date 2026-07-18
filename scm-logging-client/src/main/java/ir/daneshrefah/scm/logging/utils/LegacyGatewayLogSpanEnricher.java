package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.model.ScmResponse;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationProvider;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Component
@Slf4j
public class LegacyGatewayLogSpanEnricher {

    public static final String GATEWAY_SPAN_PROPERTY = "scm.web.legacy.gateway.log.span";
    public static final String PROVIDER_RESPONSE_RECEIVED_PROPERTY = "scm.web.legacy.gateway.log.provider.response.received";
    public static final String PROVIDER_RESPONSE_PARSE_FAILED_PROPERTY = "scm.web.legacy.gateway.log.provider.response.parse.failed";
    public static final String PROVIDER_SERVER_CODE_PROPERTY = "scm.web.legacy.gateway.log.provider.server.code";
    public static final String PROVIDER_DOC_NO_PROPERTY = "scm.web.legacy.gateway.log.provider.doc.no";
    public static final String PROVIDER_EXTERNAL_SEQUENCE_ID_PROPERTY = "scm.web.legacy.gateway.log.provider.external.sequence.id";
    public static final String PROVIDER_ORIGINAL_SEQUENCE_ID_PROPERTY = "scm.web.legacy.gateway.log.provider.original.sequence.id";

    private static final String SCM_LOG_ORIGIN = "scm.log.origin";
    private static final String SCM_LOG_BRIDGE_VERSION = "scm.log.bridge.version";
    private static final String SCM_LOG_RECORD_TYPE = "scm.log.record.type";
    private static final String SCM_WEB = "SCM_WEB";
    private static final String BRIDGE_VERSION = "8.5.4";
    private static final String GATEWAY_TRANSACTION = "GATEWAY_TRANSACTION";
    private static final String REQUEST_LOG_STATUS = "requestLogStatus";
    private static final String RESPONSE_LOG_STATUS = "responseLogStatus";
    private static final String REQUEST_TO_CHANNEL = "REQUEST_TO_CHANNEL";
    private static final String RESPONSE_FROM_CHANNEL = "RESPONSE_FROM_CHANNEL";
    private static final String RESPONSE_FAILED = "RESPONSE_FAILED";
    private static final String SERVER_CODE = "serverCode";
    private static final int TRANSACTION_TYPE_REQUEST = 1;
    private static final int TRANSACTION_TYPE_RESPONSE = 2;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> GATEWAY_SERVICE_CODES = List.of(
            "cardInquiry",
            "cardPasswordInquiry",
            "cardXferAdd"
    );




    private final ErrorMappingService errorMappingService;

    private final Logger logger;



    public LegacyGatewayLogSpanEnricher(ErrorMappingService errorMappingService, Logger logger) {
        this.errorMappingService = errorMappingService;
        this.logger = logger;
    }

    public void enrichGatewayRequest(Exchange exchange, Service service, Span span, String messageId) {
        if (exchange == null || span == null) {
            return;
        }
        if (!isScmWebExchange(exchange)) {
            return;
        }
        exchange.setProperty(GATEWAY_SPAN_PROPERTY, span);
        markScmWebSpan(span);
        span.setAttribute(SCM_LOG_RECORD_TYPE, GATEWAY_TRANSACTION);
        setString(span, LogAttribute.CORRELATION_ID.getAttributeName(), correlationId(exchange, messageId));
        setString(span, LogAttribute.CLIENT_IP_ADDRESS.getAttributeName(), clientIpAddress(exchange));
        span.setAttribute(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), TRANSACTION_TYPE_REQUEST);
        span.setAttribute(REQUEST_LOG_STATUS, REQUEST_TO_CHANNEL);

        if (!isTargetGatewayService(service)) {
            return;
        }

        JsonNode body = bodyAsJson(exchange);
        String cardNo = firstText(body,
                "fundTransfer.sourceCardNumber", "card.sourceCardNumber", "sourceCardNumber",
                "sourcePan", "cardNo", "cardNumber", "data.cardNumber");
        String destination = firstText(body,
                "fundTransfer.destinationCardNumber", "destinationCardNumber", "destinationPan",
                "destCardNo", "destCard", "destination", "data.destCard");

        setString(span, LogAttribute.CARD_NO.getAttributeName(), cardNo);
        setString(span, LogAttribute.DESTINATION.getAttributeName(), destination);
        setString(span, LogAttribute.ACCOUNT_NO.getAttributeName(), firstText(body,
                "fundTransfer.sourceAccountNumber", "sourceAccountNumber", "accountNo",
                "accountNumber", "data.accountNumber"));
        setString(span, LogAttribute.AMOUNT.getAttributeName(), firstText(body,
                "fundTransfer.amount", "amount", "data.amount"));
        setString(span, LogAttribute.INTER_BANK.getAttributeName(), interBank(body, cardNo, destination));
    }

    public void captureProviderResponse(Exchange exchange, Operation operation) {
        if (exchange == null || !isScmWebExchange(exchange) || !isShetabProviderOperation(operation)) {
            return;
        }
        JsonNode body = bodyAsJson(exchange);
        exchange.setProperty(PROVIDER_RESPONSE_RECEIVED_PROPERTY, true);

        String serverCode = firstText(body, "fields.39", "39", "serverCode", "responseCode", "resultCode");
        String rrn = firstText(body, "fields.37", "37", "rrn", "RRN", "reference", "retrievalReferenceNo");
        String originalReference = firstText(body,
                "fields.56", "56", "fields.90", "90", "originalSequenceId",
                "originalReference", "originalRrn", "originalTransactionReference", "originalTransactionId");

        setExchangeString(exchange, PROVIDER_SERVER_CODE_PROPERTY, serverCode);
        setExchangeString(exchange, PROVIDER_DOC_NO_PROPERTY, rrn);
        setExchangeString(exchange, PROVIDER_EXTERNAL_SEQUENCE_ID_PROPERTY, rrn);
        setExchangeString(exchange, PROVIDER_ORIGINAL_SEQUENCE_ID_PROPERTY, originalReference);
        if (StringUtils.isBlank(serverCode) && StringUtils.isBlank(rrn)) {
            exchange.setProperty(PROVIDER_RESPONSE_PARSE_FAILED_PROPERTY, true);
        }
    }

    public void enrichGatewayResponse(Exchange exchange, Service service, Span span) {
        if (exchange == null || span == null) {
            return;
        }
        if (!isScmWebExchange(exchange)) {
            return;
        }
        markScmWebSpan(span);
        span.setAttribute(SCM_LOG_RECORD_TYPE, GATEWAY_TRANSACTION);
        setString(span, LogAttribute.CLIENT_IP_ADDRESS.getAttributeName(), clientIpAddress(exchange));

        if (Boolean.TRUE.equals(exchange.getProperty(PROVIDER_RESPONSE_RECEIVED_PROPERTY, Boolean.class))) {
            setString(span, LogAttribute.STATUS_CODE.getAttributeName(), statusCode(exchange.getMessage().getBody()));
            setString(span, SERVER_CODE, exchange.getProperty(PROVIDER_SERVER_CODE_PROPERTY, String.class));
            setString(span, LogAttribute.DOC_NO.getAttributeName(), exchange.getProperty(PROVIDER_DOC_NO_PROPERTY, String.class));
            setString(span, LogAttribute.EXTERNAL_SEQUENCE_ID.getAttributeName(),
                    exchange.getProperty(PROVIDER_EXTERNAL_SEQUENCE_ID_PROPERTY, String.class));
            setString(span, LogAttribute.ORIGINAL_SEQUENCE_ID.getAttributeName(),
                    exchange.getProperty(PROVIDER_ORIGINAL_SEQUENCE_ID_PROPERTY, String.class));
            span.setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), TRANSACTION_TYPE_RESPONSE);
            setString(span, LogAttribute.MESSAGE_RESPONSE.getAttributeName(), bodyToString(exchange.getMessage().getBody()));
            span.setAttribute(RESPONSE_LOG_STATUS,
                    Boolean.TRUE.equals(exchange.getProperty(PROVIDER_RESPONSE_PARSE_FAILED_PROPERTY, Boolean.class))
                            ? RESPONSE_FAILED
                            : RESPONSE_FROM_CHANNEL);
        } else {
            recordException(exchange, null);
        }
    }

    public void recordException(Exchange exchange, Exception exception) {
        Span span = gatewaySpan(exchange);
        if (span == null) {
            return;
        }
        Throwable cause = exception != null ? exception : exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Object body = exchange.getMessage() != null ? exchange.getMessage().getBody() : null;
        if (cause != null) {
            setString(span, LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), cause.getClass().getName());
            setString(span, LogAttribute.DESCRIPTION.getAttributeName(), cause.getMessage());
            setString(span, LogAttribute.STATUS_CODE.getAttributeName(), statusCode(body));
        } else {
            setString(span, LogAttribute.DESCRIPTION.getAttributeName(), description(body));
            setString(span, LogAttribute.STATUS_CODE.getAttributeName(), statusCode(body));
        }
    }

    public void markScmWebSpan(Span span) {
        if (span == null) {
            return;
        }
        span.setAttribute(SCM_LOG_ORIGIN, SCM_WEB);
        span.setAttribute(SCM_LOG_BRIDGE_VERSION, BRIDGE_VERSION);
    }

    public void markScmWebSpan(Exchange exchange, Span span) {
        if (isScmWebExchange(exchange)) {
            markScmWebSpan(span);
        }
    }

    public Span gatewaySpan(Exchange exchange) {
        return exchange != null ? exchange.getProperty(GATEWAY_SPAN_PROPERTY, Span.class) : null;
    }

    public boolean isGatewaySpan(Exchange exchange, Span span) {
        return span != null && span == gatewaySpan(exchange);
    }

    public String clientIpAddress(Exchange exchange) {
        if (exchange == null || exchange.getMessage() == null) {
            return "";
        }
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        if(request != null && StringUtils.isNoneEmpty( request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }

        for (String header : List.of(
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR",
                "X-Real-IP",
                "ip"
        )) {
            String value = exchange.getMessage().getHeader(header, String.class);
            String parsed = parseClientIp(value);
            if (StringUtils.isNotBlank(parsed)) {
                return parsed;
            }
        }
        log.warn("clientIpAddress is not detected");
        return "";
    }

    private boolean isTargetGatewayService(Service service) {
        String serviceCode = service != null ? StringUtils.trimToEmpty(service.getCode()) : "";
        return GATEWAY_SERVICE_CODES.stream().anyMatch(code -> code.equalsIgnoreCase(serviceCode));
    }

    private boolean isScmWebExchange(Exchange exchange) {
        return exchange != null
                && (exchange.getProperty(GATEWAY_SPAN_PROPERTY) != null
                || exchange.getProperty(Message.GATEWAY_NAME) != null
                || exchange.getProperty(Message.GATEWAY_CHANNEL) != null
                || exchange.getProperty(Message.RUNTIME_ROUTE_PLAN) != null);
    }

    private boolean isShetabProviderOperation(Operation operation) {
        if (operation == null || operation.getType() != OperationType.PROVIDER) {
            return false;
        }
        OperationProvider provider = operation.getProvider();
        String uri = provider != null ? StringUtils.trimToEmpty(provider.getUri()) : "";
        String name = provider != null ? StringUtils.trimToEmpty(provider.getName()) : "";
        return StringUtils.startsWithIgnoreCase(uri, "scm-shetab:")
                || StringUtils.startsWithIgnoreCase(uri, "shetab:")
                || StringUtils.containsIgnoreCase(name, "shetab");
    }

    private JsonNode bodyAsJson(Exchange exchange) {
        try {
            Object body = exchange != null && exchange.getMessage() != null ? exchange.getMessage().getBody() : null;
            if (body instanceof JsonNode jsonNode) {
                return jsonNode;
            }
            if (body instanceof String text) {
                if (StringUtils.isBlank(text)) {
                    return OBJECT_MAPPER.createObjectNode();
                }
                return OBJECT_MAPPER.readTree(text);
            }
            if (body == null) {
                return OBJECT_MAPPER.createObjectNode();
            }
            return OBJECT_MAPPER.valueToTree(body);
        } catch (Exception ignored) {
            return OBJECT_MAPPER.createObjectNode();
        }
    }

    private String firstText(JsonNode root, String... paths) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return null;
        }
        for (String path : paths) {
            JsonNode node = atPath(root, path);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                String value = node.isTextual() ? node.asText() : node.asText(null);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        }
        for (String path : paths) {
            String fieldName = StringUtils.substringAfterLast(path, ".");
            JsonNode node = findField(root, fieldName);
            if (node != null && !node.isNull()) {
                String value = node.isTextual() ? node.asText() : node.asText(null);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private JsonNode atPath(JsonNode root, String path) {
        JsonNode node = root;
        for (String part : StringUtils.split(path, '.')) {
            if (node == null || node.isMissingNode() || node.isNull()) {
                return null;
            }
            node = node.path(part);
        }
        return node;
    }

    private JsonNode findField(JsonNode node, String fieldName) {
        if (node == null || fieldName == null) {
            return null;
        }
        if (node.has(fieldName)) {
            return node.get(fieldName);
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                JsonNode found = findField(fields.next().getValue(), fieldName);
                if (found != null) {
                    return found;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findField(child, fieldName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String interBank(JsonNode body, String cardNo, String destination) {
        String explicit = firstText(body, "interBank", "fundTransfer.interBank", "data.interBank");
        if (StringUtils.isNotBlank(explicit)) {
            return explicit;
        }
        if (StringUtils.length(cardNo) >= 6 && StringUtils.length(destination) >= 6) {
            return String.valueOf(!Objects.equals(cardNo.substring(0, 6), destination.substring(0, 6)));
        }
        return null;
    }

    private String correlationId(Exchange exchange, String messageId) {
        String correlationId = exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class);
        if (StringUtils.isBlank(correlationId)) {
            correlationId = exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class);
        }
        return StringUtils.defaultIfBlank(correlationId, messageId);
    }

    private String parseClientIp(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String text = value.trim();
        if (StringUtils.startsWithIgnoreCase(text, "for=")) {
            text = StringUtils.substringAfter(text, "for=");
        } else if (StringUtils.containsIgnoreCase(text, "for=")) {
            text = StringUtils.substringAfter(text, "for=");
        }
        text = StringUtils.substringBefore(text, ",");
        text = StringUtils.substringBefore(text, ";");
        text = StringUtils.strip(text, "\" ");
        if (StringUtils.startsWith(text, "/")) {
            text = text.substring(1);
        }
        if (StringUtils.startsWith(text, "[") && StringUtils.contains(text, "]")) {
            return StringUtils.substringBetween(text, "[", "]");
        }
        if (StringUtils.countMatches(text, ":") == 1 && StringUtils.isNumeric(StringUtils.substringAfterLast(text, ":"))) {
            text = StringUtils.substringBeforeLast(text, ":");
        }
        return StringUtils.strip(text, "[] ");
    }

    private String statusCode(Object body) {
        MessageStatus status = switch (body) {
            case Message message -> message.getStatus();
            case ScmFault fault -> fault.getStatus();
            case ScmResponse response -> response.getStatus();
            default -> null;
        };

        status = status != null ? status : MessageStatus.SC_SUCCESS;
        Optional<ErrorMapping> errorMapping = errorMappingService.findByStatusCode(status.getCode());
        if (errorMapping.isPresent()) {
            return String.valueOf(errorMapping.get().getScmErrorCode());
        } else {
            return status.getCode();
        }
    }

    private String description(Object body) {
        if (body instanceof ScmFault fault) {
            if (StringUtils.isNotBlank(fault.getTitle())) {
                return fault.getTitle();
            }
            if (fault.getErrors() != null && !fault.getErrors().isEmpty()) {
                Error error = fault.getErrors().getFirst();
                return StringUtils.defaultIfBlank(error.getMessage(), error.getMessageFa());
            }
        }
        return null;
    }

    private String bodyToString(Object body) {
        if (body == null) {
            return "";
        }
        try {
            JsonNode root = body instanceof String text
                    ? OBJECT_MAPPER.readTree(text)
                    : OBJECT_MAPPER.valueToTree(body);
            removeImageUrl(root);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception ignored) {
            return String.valueOf(body);
        }
    }

    private void removeImageUrl(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.remove("imageUrl");
            objectNode.fields().forEachRemaining(entry -> removeImageUrl(entry.getValue()));
            return;
        }
        if (node.isArray()) {
            node.forEach(this::removeImageUrl);
        }
    }

    private void setString(Span span, String name, String value) {
        if (span != null && StringUtils.isNotBlank(value)) {
            span.setAttribute(name, value);
        }
    }

    private void setExchangeString(Exchange exchange, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            exchange.setProperty(name, value);
        }
    }
}
