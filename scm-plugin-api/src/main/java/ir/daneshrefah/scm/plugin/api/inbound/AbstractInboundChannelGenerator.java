package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.ServiceNotFoundException;
import ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.EventType;
import ir.daneshrefah.scm.plugin.api.exception.MessageBuildException;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.utils.MessageUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ACCESS_PARAMETER;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_TERMINAL;

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

    private final ServiceProducerTemplate producerTemplate;
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
    public T execute(T input, TerminalServiceAccess serviceAccess) {
        Instant startTime = Instant.now();
        MessageBuildRequest request = new MessageBuildRequest(input);
        Message message = null;
        Exception exception = null;
        try {
            request = extractMessageBuildRequest(request, serviceAccess.getService());
            message = buildMessageInternal(request);
        } catch (Exception e) {
            message = errorHandlerService.resolveMessageByException(null, new MessageBuildException(request, serviceAccess, e));
            exception = e;
        } finally {
            logIncomingMessage(request, message, exception, startTime);
        }
        if (MessageUtils.isContinueAllowed(message)) {
            message = executeService(message);
        }
        return buildResponse(input, message);
    }

    protected T buildResponse(T input, Message message) {
        return input;
    }

    @Override
    public Message execute(MessageBuildRequest request) {
        Instant startTime = Instant.now();
        Message message = null;
        Exception exception = null;
        try {
            message = buildMessageInternal(request);
        } catch (Exception e) {
            TerminalServiceAccess serviceAccess = findServiceAccess(request.getTerminalCode(), request.getServiceCode());
            message = errorHandlerService.resolveMessageByException(null, new MessageBuildException(request, serviceAccess, e));
            exception = e;
        } finally {
            logIncomingMessage(request, message, exception, startTime);
        }
        if (!MessageUtils.isContinueAllowed(message)) {
            return message;
        }
        return executeService(message);
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
            if (!MessageUtils.isContinueAllowed(message)) {
                return message;
            }
        }

        producerTemplate.callService(message.getHeader().getServiceAccess().getService(), message);

        for (Iterator<MessageInterceptor> iterator = responseInterceptors.iterator(); iterator.hasNext(); ) {
            MessageInterceptor messageInterceptor = iterator.next();
            message = messageInterceptor.intercept(message);
            if (!MessageUtils.isContinueAllowed(message)) {
                break;
            }
        }

        return message;
    }

    private Message buildMessageInternal(MessageBuildRequest request) {
        if (StringUtils.isEmpty(request.getServiceCode())) {
            throw new ServiceNotFoundException(request.getServiceCode());
        }
        TerminalServiceAccess serviceAccess = findServiceAccess(request.getTerminalCode(), request.getServiceCode());
        if (!request.isForCheck() && null == serviceAccess) {
            throw new TerminalServiceNotFoundException(request.getTerminalCode(), request.getServiceCode());
        }
        Header header = Header.builder()
                .request(request)
                .authentication(null)
                .isTransactionAuthenticated(false)
                .correlationId(StringUtils.generateGuid())
                .channel(channel)
                .serviceAccess(serviceAccess)
                .build();

        if (!request.isForCheck() && StringUtils.isEmpty(request.getAccessParameter())) {
            throw new ValidationException(SCM_PARAMETER_ACCESS_PARAMETER, ErrorCodes.ERROR_CODE_ACCESS_PARAMETER_IS_EMPTY,
                    SCM_PARAMETER_ACCESS_PARAMETER + " is empty.");
        }
        if (!request.isForCheck() && StringUtils.isEmpty(request.getTerminalCode())) {
            throw new ValidationException(SCM_PARAMETER_TERMINAL, ErrorCodes.ERROR_CODE_TERMINAL_CODE_IS_EMPTY,
                    SCM_PARAMETER_TERMINAL + " is empty.");
        }
        if (!request.isForCheck() && !StringUtils.equals(serviceAccess.getTerminal().getCode(), request.getTerminalCode())) {
            throw new ValidationException(SCM_PARAMETER_TERMINAL, ErrorCodes.ERROR_CODE_TERMINAL_CODE_IS_INVALID,
                    SCM_PARAMETER_TERMINAL + " is invalid.");
        }

        Message message = Message.builder()
                .header(header)
                .status(request.isForCheck() ? Status.SC_SUCCESS : Status.SC_PROCESSING)
                .payload(request.getPayload())
                .build();
        MessageContext.init(message);

        return message;
    }

    private TerminalServiceAccess findServiceAccess(String terminalCode, String serviceCode) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(serviceCode))
            return null;
        if (null == serviceCode || services.size() < 1)
            return null;
        return services.stream()
                .filter(service ->
                        service.getTerminal().getCode().equals(terminalCode) &&
                                service.getService().getCode().equals(serviceCode))
                .findFirst()
                .orElse(null);
    }

    private final void logIncomingMessage(MessageBuildRequest request, Message message, Exception error, Instant startTime) {
        Instant endTime = Instant.now();
        Event event = Event.builder()
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
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

}
