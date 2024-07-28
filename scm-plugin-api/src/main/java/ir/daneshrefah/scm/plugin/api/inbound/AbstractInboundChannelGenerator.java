package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.InboundEvent;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
            try {
                message = MessageGenerator.getInstance().buildMessageFromInput();
            } catch (Exception e) {
//            TerminalServiceAccess serviceAccess = findServiceAccess(input.getHeader(SCM_PARAMETER_TERMINAL), input.getServiceCode());
                message = errorHandlerService.resolveMessageByException(MessageGenerator.getInstance().buildEmptyMessageFromInput(), e);
                exception = e;
            }
            if (message.isContinueAllowed()) {
                message = executeService(message);
            }
        } finally {
            logIncomingMessage(message, exception, startTime);
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
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        Event event = InboundEvent.builder()
                .terminalCode(messageInput.getTerminal().getCode())
                .channelCode(messageInput.getChannel().getCode())
                .clientId(messageInput.getClientId())
                .correlationId(messageInput.getCorrelationId())
                .clientCorrelationId(messageInput.getClientCorrelationId())
                .clientFlowId(messageInput.getClientFlowId())
                .serviceCode(Objects.nonNull(message.getHeader().getService()) ? message.getHeader().getService().getCode() : null)
                .username(AuthenticationUtils.getEffectiveUsername().orElse(null))
                .nickname(AuthenticationUtils.getEffectiveNickname().orElse(null))
                .delegatorUsername(AuthenticationUtils.getDelegatorUsername().orElse(null))
                .delegatorNickname(AuthenticationUtils.getDelegatorNickname().orElse(null))
                .messageId(message.getHeader().getMessageId())
                .threadName(Thread.currentThread().getName())
//        private final String hostAddress;

                .channelClassName(this.getClass().getName())
                .messageInput(messageInput)
                .messageStatus(message.getStatus())
                .errors(message.getErrors())
                .response(message.getPayload())
                .startTime(messageInput.getReceiveTimestamp())
                .endTime(Instant.now())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

}
