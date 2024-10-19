package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.model.service.ProviderTerminalCoding;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.logging.constant.LogAttribute;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.TryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-30
 */
@RequiredArgsConstructor
public abstract class AbstractExternalServiceProviderExecutor implements ExternalServiceProviderExecutor {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final ResourceService resourceService;
    private final ServiceService serviceService;
    protected final ObjectMapper objectMapper;

    @Autowired
    private  Tracer tracer;

    @Getter
    private AbstractExternalServiceProvider providerModel;

    public final void init(AbstractExternalServiceProvider provider) {
        this.providerModel = provider;
    }

    protected String extractProviderEndpoint() {
        if (null == providerModel || null == providerModel.getMetadata() || StringUtils.isEmpty(providerModel.getMetadata().getEndpoint())) {
            return null;
        }
        String result = resourceService.prepareProperties(providerModel.getMetadata().getEndpoint());
        return StringUtils.appendIfMissing(result, "/");
    }

    @Override
    public final void intiEndpointCallRouteDefinition(RouteDefinition routeDefinition) {
        TryDefinition tryDefinition = routeDefinition.doTry();
        tryDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            MessageOutput messageOutput = buildMessageOutput();
            messageOutput.setExternalCorrelationId(extractProviderCorrelationId(originalMessage));

            Object body = exchange.getMessage().getBody();
            AbstractExternalService service = (AbstractExternalService) originalMessage.getHeader().getService();
            if (Objects.nonNull(service.getRequestBodyType())) {
                switch (service.getRequestBodyType()) {
                    case NONE:
                        body = null;
                        break;
                    case MESSAGE_BODY:
                        break;
                    case PARAMETERS:
                        body = extractServiceParametersRequestBody(originalMessage, body, messageOutput);
                        break;
                }
            }

            messageOutput.setBody(body);

            exchange.getMessage().setBody(messageOutput.getBody());

            exchange.setProperty(HEADER_MESSAGE_OUTPUT, messageOutput);
//            exchange.setProperty(HEADER_START_TIME, Instant.now());
        });
        intiEndpointCallRouteDefinitionInternal(tryDefinition);
        tryDefinition.doFinally();
        tryDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            AbstractExternalService service = (AbstractExternalService) originalMessage.getHeader().getService();
            Exception exception = extractException(exchange);
            MessageInput messageInput = MessageInputContext.getCurrentContext();
            MessageOutput messageOutput = exchange.getProperty(HEADER_MESSAGE_OUTPUT, MessageOutput.class);
            Span span = tracer.spanBuilder(messageInput.getServiceCode()).setSpanKind(SpanKind.CLIENT).startSpan();
            try (Scope rootScope = span.makeCurrent()) {
                span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), messageInput.getTerminal().getCode());
                span.setAttribute(LogAttribute.CHANNEL_CODE.getAttributeName(), messageInput.getChannel().getCode());
                span.setAttribute(LogAttribute.CLIENT_ID.getAttributeName(), messageInput.getClientId());
                span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), messageInput.getCorrelationId());
                span.setAttribute(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), messageInput.getClientCorrelationId());
                span.setAttribute(LogAttribute.CLIENT_FLOW_ID.getAttributeName(), messageInput.getClientFlowId());
                span.setAttribute(LogAttribute.FLOW_ID.getAttributeName(), messageInput.getFlowId());
                span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), originalMessage.getHeader().getMessageId());
                span.setAttribute(LogAttribute.SERVICE_CODE.getAttributeName(), service.getCode());
                span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
                span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
                span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
                span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
                span.setAttribute(LogAttribute.THREAD_NAME.getAttributeName(), Thread.currentThread().getName());
                span.setAttribute(LogAttribute.CHANNEL_CLASS_NAME.getAttributeName(), this.getClass().getName());
                span.setAttribute(LogAttribute.PROVIDER_CODE.getAttributeName(), service.getServiceProvider().getCode());
                span.setAttribute(LogAttribute.PROVIDER_TARGET_URL.getAttributeName(), messageOutput.getProviderUrl());
                span.setAttribute(LogAttribute.PROVIDER_PROTOCOL.getAttributeName(), messageOutput.getProtocol().name());
                span.setAttribute(LogAttribute.REQUEST_BODY_TYPE.getAttributeName(), messageOutput.getBodyType());
                String requestHeaders = messageOutput.getHeaders().entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", "));
                span.setAttribute(LogAttribute.REQUEST_HEADERS.getAttributeName(), requestHeaders);
                span.setAttribute(LogAttribute.RESPONSE_BODY_TYPE.getAttributeName(), null != exchange.getMessage().getBody() ? exchange.getMessage().getBody().getClass().getName() : "null");
                String responseHeader = exchange.getMessage().getHeaders().entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(", "));
                span.setAttribute(LogAttribute.RESPONSE_HEADERS.getAttributeName(), responseHeader);
                span.setAttribute(LogAttribute.RESPONSE.getAttributeName(), exchange.getMessage().getBody(String.class));
                span.setAttribute(LogAttribute.REQUEST.getAttributeName(), objectMapper.writeValueAsString(messageOutput.getBody()));
                span.setStatus(StatusCode.OK);
                span.recordException(exception);
            } catch (JsonProcessingException ex) {
                span.setStatus(StatusCode.ERROR);
                span.recordException(ex);
            }finally {
                span.end();
            }
            if (ExternalServiceBodyType.PARAMETERS.equals(service.getRequestBodyType())) {
                Object header = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE);
                header = Objects.isNull(header) ? -1 : header;
                originalMessage.getHeader().getHttpHeader().setHttpStatusCode(Integer.parseInt(String.valueOf(header)));
                exchange.getMessage().setBody(extractServiceParametersResponseBody(originalMessage, exchange.getMessage().getBody()));
                Map<String, ?> responseHeaders = extractResponseHeaders(originalMessage);
                Set<String> headersNames = responseHeaders.keySet();
                for (String headersName : headersNames) {
                    exchange.getMessage().setHeader(headersName,responseHeaders.get(headersName));
                }
            }
        });
        tryDefinition.endDoTry();
    }

    protected String extractProviderCorrelationId(Message message) {
        return MessageInputContext.getCurrentContext().getCorrelationId();
    }

    private Exception extractException(Exchange exchange) {
        Exception exception = exchange.getException();
        if (null == exception) {
            exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }
        if (null == exception) {
            exception = exchange.getProperty(Exchange.EXCEPTION_HANDLED, Exception.class);
        }
        return exception;
    }

    protected abstract MessageOutput buildMessageOutput();

    protected Object extractServiceParametersResponseBody(Message message, Object body) {
        return null;
    }

    protected Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput) {
        return null;
    }

    protected Map<String, ?> extractResponseHeaders(Message message){
        return Collections.emptyMap();
    }

    protected abstract void intiEndpointCallRouteDefinitionInternal(TryDefinition routeDefinition);

//    public List<TransformerExecutionWrapper> getRequestTransformers() {
//        return Collections.emptyList();
//    }
//
//    public List<TransformerExecutionWrapper> getResponseTransformers() {
//        return Collections.emptyList();
//    }

    protected Optional<Object> extractParameterValue(Message message, Parameter parameter) {
        return ParameterDataProvider.getInstance().extractParameterValue(message, parameter);
    }

    protected final Optional<String> prepareTerminalCode(AbstractExternalService service, String defaultValue) {
        String terminalCode = AuthenticationUtils.getLoggedInTerminalCode().orElse(null);
        String clientId = AuthenticationUtils.getLoggedInClientId().orElse(null);
        String providerCode = service.getServiceProvider().getAssetProvider().getCode().getValue();
        Optional<ProviderTerminalCoding> providerTerminalCoding = serviceService.findProviderTerminalCoding(terminalCode, clientId, providerCode);
        return providerTerminalCoding.flatMap(po -> Optional.ofNullable(po.getCode()))
                .or(() -> Optional.ofNullable(defaultValue));
    }

}
