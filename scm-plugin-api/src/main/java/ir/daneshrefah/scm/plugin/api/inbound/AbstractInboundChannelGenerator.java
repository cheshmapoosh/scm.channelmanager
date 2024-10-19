package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.logging.utils.TraceLogUtils;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    @Autowired
    private Tracer tracer;
    @Autowired
    private TraceLogUtils traceLogUtils;

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

    protected boolean initConfig() {
        return true;
    }

    @Override
    public Message execute() {
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        Span rootSpan = tracer.spanBuilder(messageInput.getServiceCode())
                .setSpanKind(SpanKind.SERVER).setNoParent()
                .startSpan()
                .setStatus(StatusCode.OK);
        Message message = null;
        Exception exception = null;
        try {
            try {
                message = MessageGenerator.getInstance().buildMessageFromInput();
            } catch (Exception e) {
//            TerminalServiceAccess serviceAccess = findServiceAccess(input.getHeader(SCM_PARAMETER_TERMINAL), input.getServiceCode());
                message = errorHandlerService.resolveMessageByException(MessageGenerator.getInstance().buildEmptyMessageFromInput(), e);
                exception = e;
            } finally {
                traceLogUtils.recordMessageTrace(message, null, exception, rootSpan);
                rootSpan.makeCurrent();
            }
            if (message.isContinueAllowed()) {
                message = executeService(message);
            }
        } catch (Exception ex) {
            rootSpan.setStatus(StatusCode.ERROR);
            rootSpan.recordException(ex);
        }finally {
            rootSpan.end();
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
}
