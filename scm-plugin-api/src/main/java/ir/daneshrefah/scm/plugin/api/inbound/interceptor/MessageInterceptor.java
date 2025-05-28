package ir.daneshrefah.scm.plugin.api.inbound.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@Slf4j
public abstract class MessageInterceptor {

    public final Message intercept(Message message) {
        Instant startTime = Instant.now();
        Exception error = null;
        Message orgMessage = MessageGenerator.getInstance().cloneMessage(message);
        try {
            boolean isSupported = support(message.getHeader().getService());
            if (isSupported) {
                message = internalIntercept(message);
            }
        } catch (Exception e) {
            error = e;
            throw e;
        } finally {
            logMessageInterceptor(orgMessage, message, error, startTime);
        }

        return message;
    }

    @PostConstruct
    public void init(){
        log.info("[ {} ] Message Interceptor has benn initialized", this.getClass().getName());
    }

    protected abstract Message internalIntercept(Message message);

    protected abstract boolean support(ScmService service);

    public abstract InterceptorConfig interceptorConfig();

    private void logMessageInterceptor(Message orgMessage, Message message, Exception error, Instant startTime) {
        Instant endTime = Instant.now();
        /*Event event = Event.builder()
                .type(EventType.INTERCEPTOR)
                .status(null != message ? message.getStatus() : null)
                .correlationId(message.getHeader().getCorrelationId())
                .source(this.getClass().getSimpleName())
                .terminalCode(message.getHeader().getTerminalCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(orgMessage)
                .output(message)
                .error(error)
                .sourceClassName(this.getClass().getSimpleName())
                .build();*/
//        EventProducer.getInstance().sendEvent(event);
    }
}
