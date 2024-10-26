package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ProviderTerminalCoding;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.logging.constant.LogAttribute;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStep;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.TryDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractBaseExternalServiceProviderExecutor implements ExternalServiceProviderExecutor {


    protected final ObjectMapper objectMapper;
    private final ResourceService resourceService;
    private final ServiceService serviceService;
    @Autowired
    private Tracer tracer;

    @Getter
    private AbstractExternalServiceProvider serviceProvider;

    public final void init(AbstractExternalServiceProvider provider) {
        this.serviceProvider = provider;
    }

    @Override
    public void endpointCallRouteDefinition(RouteDefinition routeDefinition) {
        TryDefinition tryDefinition = routeDefinition.doTry();
        tryDefinition.process(this::beforeRouteCalling);
        callRoute(tryDefinition).forEach(camelInvocationStep -> camelInvocationStep.call(tryDefinition));
        tryDefinition.process(this::afterRouteCalling);
        tryDefinition.doFinally();
        tryDefinition.process(this::logResult);
        tryDefinition.endDoTry();
    }

    protected abstract void beforeRouteCalling(Exchange exchange);

    protected abstract void afterRouteCalling(Exchange exchange);

    protected abstract List<CamelInvocationStep> callRoute(TryDefinition routeDefinition);

    private void logResult(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        AbstractExternalService<?> service = (AbstractExternalService<?>) originalMessage.getHeader().getService();
        Instant endTime = Instant.now();
        executeEventLog(service, originalMessage, exchange);
    }

    private Exception extractException(Exchange exchange) {
        Exception exception = exchange.getException();
        if (Objects.isNull(exception)) {
            exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }
        if (Objects.isNull(exception)) {
            exception = exchange.getProperty(Exchange.EXCEPTION_HANDLED, Exception.class);
        }
        return exception;
    }

    private void executeEventLog(AbstractExternalService<?> service, Message originalMessage, Exchange exchange) {
        MessageInput<?> messageInput = MessageInputContext.getCurrentContext();
        MessageOutput messageOutput = exchange.getProperty(HEADER_MESSAGE_OUTPUT, MessageOutput.class);
        Exception exception = extractException(exchange);
        Span span = tracer.spanBuilder(messageInput.getServiceCode()).setSpanKind(SpanKind.CLIENT).startSpan();
        Scope rootScope = span.makeCurrent();
        try (rootScope) {
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
    }


    public String getProviderCorrelationId(Message message) {
        return MessageInputContext.getCurrentContext().getCorrelationId();
    }

    protected Optional<String> getProviderEndpoint() {
        if (Objects.nonNull(serviceProvider)
            && Objects.nonNull(serviceProvider.getMetadata())
            && StringUtils.isNotEmpty(serviceProvider.getMetadata().getEndpoint())) {
            String result = resourceService.prepareProperties(serviceProvider.getMetadata().getEndpoint());
            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }

    protected final Optional<String> prepareTerminalCode(AbstractExternalService<?> service, String defaultValue) {
        String terminalCode = AuthenticationUtils.getLoggedInTerminalCode().orElse(null);
        String clientId = AuthenticationUtils.getLoggedInClientId().orElse(null);
        String providerCode = service.getServiceProvider().getAssetProvider().getCode().getValue();
        Optional<ProviderTerminalCoding> providerTerminalCoding = serviceService.findProviderTerminalCoding(terminalCode, clientId, providerCode);
        return providerTerminalCoding.flatMap(po -> Optional.ofNullable(po.getCode()))
                .or(() -> Optional.ofNullable(defaultValue));
    }

    public AbstractExternalService<?> getService(Exchange exchange){
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        return (AbstractExternalService<?>) originalMessage.getHeader().getService();
    }

    @Override
    public AbstractExternalServiceProvider getProviderModel() {
        return this.serviceProvider;
    }


}
