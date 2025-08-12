package ir.daneshrefah.scm.logging.utils;

import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.camel.Exchange;
import org.apache.camel.tracing.ActiveSpanManager;
import org.apache.camel.tracing.SpanAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TraceUtils {

    @Getter
    private static TraceUtils instance;
    @Value("${scm.application.verion:#{null}}")
    private String version;
    @Value("${scm.application.build:#{null}}")
    private String build;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public void traceBeforeRoute(Exchange exchange, Service service) {
        SpanAdapter span = ActiveSpanManager.getSpan(exchange);
        span.setTag(LogAttribute.REQUEST.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : null);
        trace(exchange, service, span);
    }

    public void traceAfterRoute(Exchange exchange, Service service) {
        SpanAdapter span = ActiveSpanManager.getSpan(exchange);
        span.setTag(LogAttribute.RESPONSE.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : null);
        trace(exchange, service, span);
    }

    public void traceException(Exchange exchange, Exception exception) {
        SpanAdapter span = ActiveSpanManager.getSpan(exchange);
        span.setTag(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), exception.getClass().getName());
        span.setTag(LogAttribute.ERROR_DETAILS.getAttributeName(), exception.getMessage());
        span.setError(true);
    }

    private void trace(Exchange exchange, Service service, SpanAdapter span) {
        span.setTag(LogAttribute.CHANNEL_CODE.getAttributeName(), exchange.getProperty(Message.CHANNEL_CODE) != null ? exchange.getProperty(Message.CHANNEL_CODE).toString() : null);
        span.setTag(LogAttribute.CLIENT_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_ID, String.class));
        span.setTag(LogAttribute.TERMINAL_CODE.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_TERMINAL, String.class));
        span.setTag(LogAttribute.CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class));
        span.setTag(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class));
        span.setTag(LogAttribute.FLOW_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_FLOW_ID, String.class));
        span.setTag(LogAttribute.HOST_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HEADER_HOST, String.class));
        span.setTag(LogAttribute.SERVICE_CODE.getAttributeName(), service.getCode() != null ? service.getCode().trim() : null);
        span.setTag(LogAttribute.END_POINT.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class));
        span.setTag(LogAttribute.METHOD_TYPE.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class));
        span.setTag(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_REFERER, String.class));
        span.setTag(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_USERNAME, String.class));
        span.setTag(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(null));
        span.setTag(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(null));
        span.setTag(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(null));
        span.setTag(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(null));
        span.setTag(LogAttribute.VERSION.getAttributeName(), version + build);
    }
}