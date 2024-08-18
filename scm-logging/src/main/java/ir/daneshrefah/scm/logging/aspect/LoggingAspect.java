package ir.daneshrefah.scm.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.event.InboundEvent;
import ir.daneshrefah.scm.common.model.event.OutboundEvent;
import ir.daneshrefah.scm.common.model.message.HttpMessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
@ConditionalOnProperty(name = "scm.log.aspect.enable", havingValue = "true")
public class LoggingAspect {

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController)    &&" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)    ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)    ||" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping)   ||" +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)  ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)&& " +
            "!@annotation(ir.daneshrefah.scm.logging.aspect.annotation.SkipLog)")
    private Object logAdviser(ProceedingJoinPoint proceed) throws Throwable {
        Instant startTime = Instant.now();
        UUID uuid = UUID.randomUUID();
        Object returnValue;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        try {
            beforeAdvice(requestAttributes, uuid, startTime);
            returnValue = proceed.proceed();
            afterAdvice(requestAttributes, returnValue, uuid, startTime);
        } catch (Throwable ex) {
            EventProducer eventProducer = EventProducer.getInstance();
            OutboundEvent outboundEvent = OutboundEvent.builder()
                    .startTime(startTime)
                    .endTime(Instant.now())
                    .correlationId(uuid.toString())
                    .exception((Exception) ex)
                    .build();
            eventProducer.sendEvent(outboundEvent);
            throw ex;
        }
        return returnValue;
    }

    public void beforeAdvice(RequestAttributes requestAttributes, UUID uuid, Instant startTime) {
        EventProducer eventProducer = EventProducer.getInstance();
        InboundEvent inboundEvent = new InboundEvent();
        try {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            inboundEvent = InboundEvent.builder()
                    .username(AuthenticationUtils.getEffectiveUsername().orElse(null))
                    .nickname(AuthenticationUtils.getEffectiveNickname().orElse(null))
                    .delegatorUsername(AuthenticationUtils.getDelegatorUsername().orElse(null))
                    .delegatorNickname(AuthenticationUtils.getDelegatorNickname().orElse(null))
                    .correlationId(uuid.toString())
                    .threadName(Thread.currentThread().getName())
                    .hostAddress(request.getRemoteAddr())
                    .messageInput(generateHttpMessageInput(request))
                    .startTime(startTime)
                    .endTime(Instant.now())
                    .messageStatus(MessageStatus.SC_SUCCESS)
                    .build();
        } catch (Exception ex) {
            inboundEvent.setMessageStatus(MessageStatus.SC_ERROR_SYSTEM);
            inboundEvent.setException(ex);
        }
        eventProducer.sendEvent(inboundEvent);
    }

    public void afterAdvice(RequestAttributes requestAttributes, Object returnValue, UUID uuid, Instant startTime) {
        EventProducer eventProducer = EventProducer.getInstance();
        OutboundEvent outboundEvent = new OutboundEvent();
        try {
            HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            outboundEvent = OutboundEvent.builder().correlationId(uuid.toString())
                    .username(AuthenticationUtils.getEffectiveUsername().orElse(null))
                    .nickname(AuthenticationUtils.getEffectiveNickname().orElse(null))
                    .nickname(AuthenticationUtils.getEffectiveNickname().orElse(null))
                    .delegatorUsername(AuthenticationUtils.getDelegatorUsername().orElse(null))
                    .delegatorNickname(AuthenticationUtils.getDelegatorNickname().orElse(null))
                    .threadName(Thread.currentThread().getName())
                    .providerResponseCode(String.valueOf(response.getStatus()))
                    .responseHeaders(getResponseHeadersAsMap(response))
                    .requestHeaders(getRequestHeadersAsMap(request))
                    .responseBody(getBodyAsString(returnValue))
                    .hostAddress(request.getRemoteAddr())
                    .startTime(startTime)
                    .endTime(Instant.now()).build();
        } catch (Exception ex) {
            outboundEvent.setException(ex);
        }
        eventProducer.sendEvent(outboundEvent);
    }

    public HttpMessageInput generateHttpMessageInput(HttpServletRequest request) {
        String requestBody = null;
        if (request instanceof ContentCachingRequestWrapper contentCachingRequestWrapper) {
            requestBody = new String(contentCachingRequestWrapper.getContentAsByteArray());
        }
        return HttpMessageInput.builder()
                .httpMethod(request.getMethod())
                .httpUrl(request.getRequestURL() != null ? request.getRequestURL().toString() : null)
                .clientAgent(request.getHeader("User-Agent"))
                .requestBody(requestBody)
                .build();
    }

    public static Map<String, Object> getRequestHeadersAsMap(HttpServletRequest request) {
        try {
            return Collections
                    .list(request.getHeaderNames())
                    .stream()
                    .collect(Collectors.toMap(headerName -> headerName, request::getHeader));
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> getResponseHeadersAsMap(HttpServletResponse response) {
        try {
            Map<String, Object> responseHeaderMap = new HashMap<>();
            Collection<String> headerNames = response.getHeaderNames();
            for (String header : headerNames) {
                responseHeaderMap.put(header, response.getHeader(header));
            }
            return responseHeaderMap;
        } catch (Exception e) {
            return null;
        }
    }

    public String getBodyAsString(Object inputArgs) {
        try {
            return objectMapper.writeValueAsString(inputArgs);
        } catch (Exception e) {
            return null;
        }
    }
}


