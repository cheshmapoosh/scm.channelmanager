package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractInboundChannelGenerator<T> implements InboundChannelGenerator<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractInboundChannelGenerator.class);

    private final ServiceProducerTemplate producerTemplate;
    private final MessageGenerator messageGenerator;
    protected final ErrorHandlerService errorHandlerService;
    protected final ObjectMapper objectMapper;
    @Getter(AccessLevel.PROTECTED)
    private Channel channel;
    @Getter(AccessLevel.PROTECTED)
    private JsonNode metadata;
    @Getter(AccessLevel.PROTECTED)
    private List<TerminalServiceAccess> services;

    private List<MessageInterceptor> requestInterceptors;
    private List<MessageInterceptor> responseInterceptors;

    public final boolean initConfig(List<MessageInterceptor> requestInterceptors,
                                    List<MessageInterceptor> responseInterceptors,
                                    Channel channel, JsonNode metadata) {
        this.channel = channel;
        this.metadata = metadata;
        this.requestInterceptors = requestInterceptors;
        this.responseInterceptors = responseInterceptors;

        return initConfig();
    }

    @Override
    public final boolean registerEndpoints(List<TerminalServiceAccess> services) {
        this.services = services;
        return registerEndpoints();
    }

    @Override
    public Message execute(MessageInput input) {
        Instant startTime = Instant.now();
        Message message = null;
        Exception exception = null;
        try {
            message = messageGenerator.buildMessageInternal(input, channel);
        } catch (Exception e) {
//            TerminalServiceAccess serviceAccess = findServiceAccess(input.getHeader(SCM_PARAMETER_TERMINAL), input.getServiceCode());
            message = errorHandlerService.resolveMessageByException(messageGenerator.buildEmptyMessage(input, channel, e), e);
            exception = e;
        } finally {
//            logIncomingMessage(request, message, exception, startTime);
        }
        if (message.isContinueAllowed()) {
            message = executeService(message);
        }
        return message;
    }

    private Message executeService(Message message) {
        try {
            return executeServiceInternal(message);
        } catch (Exception e) {
            return errorHandlerService.resolveMessageByException(message, e);
        } finally {

        }
    }

    private Message executeServiceInternal(Message message) {
        for (Iterator<MessageInterceptor> iterator = requestInterceptors.iterator(); iterator.hasNext(); ) {
            MessageInterceptor messageInterceptor = iterator.next();
            message = messageInterceptor.intercept(message);
            if (!message.isContinueAllowed()) {
                return message;
            }
        }

        producerTemplate.callService(message.getHeader().getServiceAccess().getService(), message);

        for (Iterator<MessageInterceptor> iterator = responseInterceptors.iterator(); iterator.hasNext(); ) {
            MessageInterceptor messageInterceptor = iterator.next();
            message = messageInterceptor.intercept(message);
            if (!message.isContinueAllowed()) {
                break;
            }
        }

        return message;
    }

    private final void logIncomingMessage(MessageBuildRequest request, Message message, Exception error, Instant startTime) {
        Instant endTime = Instant.now();
        /*Event event = Event.builder()
                .type(EventType.INBOUND)
                .status(null != message ? message.getStatus() : null)
                .correlationId(message.getHeader().getCorrelationId())
                .source(request.getServiceCode())
                .terminalCode(request.getTerminalCode())
                .channelCode(getChannel().getCode())
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(request)
                .output(null != message ? message.getPayload() : null)
                .error(error)
                .sourceClassName(this.getClass().getSimpleName())
                .build();*/
//        EventProducer.getInstance().sendEvent(event);
    }

}
