package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.plugin.api.inbound.ResponseBuilder;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class CamelHttpResponseBuilder implements ResponseBuilder<Exchange> {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private static Map<Status, Integer> statusMappingMap = new HashMap<>();
    static {
        statusMappingMap.put(Status.SC_PROCESSING, 500);
        statusMappingMap.put(Status.SC_SUCCESS, 200);
        statusMappingMap.put(Status.SC_ACCESS_DENIED, 403);
        statusMappingMap.put(Status.SC_UNAUTHORIZED, 401);
        statusMappingMap.put(Status.SC_NOT_FOUND, 404);
        statusMappingMap.put(Status.SC_ERROR_VALIDATION, 400);
        statusMappingMap.put(Status.SC_ERROR_DATA_INTEGRITY_VIOLATION, 400);
        statusMappingMap.put(Status.SC_ERROR_SYSTEM, 500);
        statusMappingMap.put(Status.SC_ERROR_BUSINESS, 400);
        statusMappingMap.put(Status.SC_ERROR_UNREACHABLE_PROVIDER, 502);
    }

    private final ObjectMapper objectMapper;

    public CamelHttpResponseBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exchange build(Exchange input, Message  message) {
        Exchange response = input;
        org.apache.camel.Message responseMessage = response.getMessage();

        prepareResponseHeader(message, responseMessage);
        ObjectNode result = objectMapper.createObjectNode();
        result.putPOJO("status", message.getStatus());
        result.putPOJO("errors", message.getErrors());
        result.set("result", message.getPayload());
        responseMessage.setBody(result);

        return response;
    }

    private void prepareResponseHeader(Message message, org.apache.camel.Message responseMessage) {
        String contentType = message.getHeader().getContentType();
        if (StringUtils.isEmpty(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        responseMessage.setHeader(Exchange.HTTP_RESPONSE_CODE, statusMappingMap.get(message.getStatus()));
        responseMessage.setHeader(Exchange.CONTENT_TYPE, contentType);
        responseMessage.setHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, message.getHeader().getClientCorrelationId());
        responseMessage.setHeader(Constants.SCM_PARAMETER_CORRELATION_ID, message.getHeader().getCorrelationId());
        responseMessage.setHeader(Constants.SCM_PARAMETER_CLIENT_TIMESTAMP, message.getHeader().getClientTimestamp());
        responseMessage.setHeader(Constants.SCM_PARAMETER_RECEIVE_TIMESTAMP, message.getHeader().getReceiveTimestamp());
        Instant responseTime = Instant.now();
        responseMessage.setHeader(Constants.SCM_PARAMETER_RESPONSE_TIMESTAMP, responseTime);
        String duration = null;
        if (null != message.getHeader().getReceiveTimestamp()) {
            duration = Duration.between(message.getHeader().getReceiveTimestamp(), responseTime).toSeconds() + "(s)";
        }
        responseMessage.setHeader(Constants.SCM_PARAMETER_RESPONSE_DURATION, duration);
//        exchange.getMessage().setHeader("Access-Control-Allow-Credentials", "true");
//        exchange.getMessage().setHeader("Access-Control-Allow-Headers", "*");
//        exchange.getMessage().setHeader("Access-Control-Allow-Methods", "*");
//        exchange.getMessage().setHeader("Access-Control-Allow-Origin", "*");
//        exchange.getMessage().setHeader("Access-Control-Max-Age", "3600");*/
    }

}
