package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceRequestBodyType;
import ir.daneshrefah.scm.common.model.service.ProviderTerminalCoding;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.common.model.event.OutboundEvent;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
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

import java.time.Instant;
import java.util.*;

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
            Instant endTime = Instant.now();

            MessageInput messageInput = MessageInputContext.getCurrentContext();
            MessageOutput messageOutput = exchange.getProperty(HEADER_MESSAGE_OUTPUT, MessageOutput.class);

            OutboundEvent event = OutboundEvent.builder()
                    .terminalCode(messageInput.getTerminal().getCode())
                    .channelCode(messageInput.getChannel().getCode())
                    .clientId(messageInput.getClientId())
                    .correlationId(messageInput.getCorrelationId())
                    .clientCorrelationId(messageInput.getClientCorrelationId())
                    .clientFlowId(messageInput.getClientFlowId())
                    .serviceCode(service.getCode())
                    .username(AuthenticationUtils.getEffectiveUsername().orElse(null))
                    .nickname(AuthenticationUtils.getEffectiveNickname().orElse(null))
                    .delegatorUsername(AuthenticationUtils.getDelegatorUsername().orElse(null))
                    .delegatorNickname(AuthenticationUtils.getDelegatorNickname().orElse(null))
                    .messageId(originalMessage.getHeader().getMessageId())
                    .threadName(Thread.currentThread().getName())
                    .providerClassName(this.getClass().getName())
                    .hostAddress(null)
                    .providerCode(service.getServiceProvider().getCode())
                    .providerTargetUrl(messageOutput.getProviderUrl())
                    .providerProtocol(messageOutput.getProtocol().name())
                    .requestBody(messageOutput.getBody())
                    .requestBodyType(messageOutput.getBodyType())
                    .requestHeaders(messageOutput.getHeaders())
                    .responseBody(exchange.getMessage().getBody(String.class))
                    .responseBodyType(null != exchange.getMessage().getBody() ? exchange.getMessage().getBody().getClass().getName() : "null")
                    .responseHeaders(exchange.getMessage().getHeaders())
                    .exception(exception)
                    .startTime(messageOutput.getStartTime())
                    .endTime(endTime)
                    .build();
//            EventProducer.getInstance().sendEvent(event);

            if (ExternalServiceRequestBodyType.PARAMETERS.equals(service.getRequestBodyType())) {
                originalMessage.getHeader().getHttpHeader().setHttpStatusCode(Integer.parseInt(String.valueOf(exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE))));
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
        String providerCode = service.getServiceProvider().getAssetProvider().getCode();
        Optional<ProviderTerminalCoding> providerTerminalCoding = serviceService.findProviderTerminalCoding(terminalCode, clientId, providerCode);
        return providerTerminalCoding.flatMap(po -> Optional.ofNullable(po.getCode()))
                .or(() -> Optional.ofNullable(defaultValue));
    }

}
