package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
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
public abstract class AbstractInboundChannelGenerator implements InboundChannelGenerator {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractInboundChannelGenerator.class);

    private final ServiceProducerTemplate producerTemplate;
    protected final ErrorHandlerService errorHandlerService;
    protected final ObjectMapper objectMapper;
    @Getter(AccessLevel.PROTECTED)
    private Channel channel;
    @Getter(AccessLevel.PROTECTED)
    private List<TerminalServiceAccess> services;

    public final boolean initConfig(Channel channel, List<TerminalServiceAccess> services) {
        this.channel = channel;
        boolean initConfig = initConfig();
        if (!initConfig) {
            return false;
        }
        return registerEndpoints(services);
    }

    public final boolean registerEndpoints(List<TerminalServiceAccess> services) {
        this.services = services;
        return registerEndpoints();
    }

    protected abstract boolean registerEndpoints();

    protected boolean initConfig() {return true;}

    @Override
    public Message execute() {
        Instant startTime = Instant.now();
        Message message = null;
        Exception exception = null;
        try {
            message = MessageGenerator.getInstance().buildMessageFromInput();
        } catch (Exception e) {
//            TerminalServiceAccess serviceAccess = findServiceAccess(input.getHeader(SCM_PARAMETER_TERMINAL), input.getServiceCode());
            message = errorHandlerService.resolveMessageByException(MessageGenerator.getInstance().buildEmptyMessageFromInput(), e);
            exception = e;
        } finally {
            logIncomingMessage(message, exception, startTime);
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
        producerTemplate.callService(message.getHeader().getService(), message);

        return message;
    }

    private final void logIncomingMessage(Message message, Exception error, Instant startTime) {
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
