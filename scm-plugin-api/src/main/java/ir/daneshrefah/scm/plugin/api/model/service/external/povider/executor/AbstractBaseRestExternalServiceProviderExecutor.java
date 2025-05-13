package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.HttpMessageOutput;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractAuditableExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;

import java.util.*;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE;

public abstract class AbstractBaseRestExternalServiceProviderExecutor extends AbstractSingleStepExternalServiceProviderExecutor {

    private static final String HEADER_TARGET_URL = "ScmTargetUrl";

    public AbstractBaseRestExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }


    //REST STANDARD PART
    protected abstract Optional<Map<String, ?>> extractRequestHeaders(Message message);

    protected abstract Optional<Map<String, ?>> extractResponseHeaders(Message message);

    protected abstract HttpMethod extractHttpMethod(Message message);

    protected abstract HttpContentType extractContentType(Message message);

    protected abstract String extractTargetUrl(Message message);

    protected abstract Optional<String> extractQueryString(Message message);
    //REQUEST BODY AND RESPONSE BODY ON SUPER CLASS HAS BEEN INITIALIZED.


    @Override
    public void call(TryDefinition tryDefinition) {
        tryDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            MessageOutput messageOutput = exchange.getProperty(HEADER_MESSAGE_OUTPUT, MessageOutput.class);
            messageOutput.setProviderUrl(extractTargetEndpointUrl(originalMessage));
            messageOutput.setHeaders(extractCamelHeaders(originalMessage));
            String providerUrl = messageOutput.getProviderUrl();
            exchange.getMessage().setHeader(HEADER_TARGET_URL, providerUrl);
            Map<String, Object> headers = messageOutput.getHeaders();
            if (null != headers && !headers.isEmpty()) {
                for (String header : headers.keySet()) {
                    exchange.getMessage().setHeader(header, headers.get(header));
                }
            }

        });
        tryDefinition.toD("${header." + HEADER_TARGET_URL + "}");
    }

    @Override
    protected void afterCallRoute(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        AbstractAuditableExternalService<?> service = (AbstractAuditableExternalService<?>) originalMessage.getHeader().getService();
        if (ExternalServiceBodyType.PARAMETERS.equals(service.getRequestBodyType())) {
            Object header = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE);
            header = Objects.isNull(header) ? -1 : header;
            originalMessage.getHeader().getHttpHeader().setHttpStatusCode(Integer.parseInt(String.valueOf(header)));
            exchange.getMessage().setBody(extractServiceParametersResponseBody(originalMessage, exchange.getMessage().getBody()));
            extractResponseHeaders(originalMessage).ifPresent(responseHeaders -> {
                Set<String> headersNames = responseHeaders.keySet();
                for (String headersName : headersNames) {
                    exchange.getMessage().setHeader(headersName, responseHeaders.get(headersName));
                }
            });
        }
    }

    private Map<String, Object> extractCamelHeaders(Message message) {
        Map<String, Object> headers = new HashMap<>();
        extractRequestHeaders(message).ifPresent(headers::putAll);
        extractResponseHeaders(message).ifPresent(headers::putAll); /* MAYBE NEEDS TO CHANGE */
        headers.put(Exchange.HTTP_METHOD, extractHttpMethod(message).getValue());
        headers.put(HTTP_HEADER_CONTENT_TYPE, extractContentType(message).getValue());
        extractQueryString(message).ifPresent(queryString -> headers.put(Exchange.HTTP_QUERY, queryString));
        return headers;
    }

    @Override
    protected MessageOutput buildMessageOutput() {
        return HttpMessageOutput.builder().build();
    }

    protected final String extractTargetEndpointUrl(Message message) {
        AbstractAuditableExternalServiceProvider provider = ((AbstractAuditableExternalService<?>) message.getHeader().getService()).getServiceProvider();
        String targetUrl = extractTargetUrl(message);
        if (null != provider.getMetadata() && null != provider.getMetadata().getConnectTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "connectTimeout", provider.getMetadata().getConnectTimeout());
        }
        if (null != provider.getMetadata() && null != provider.getMetadata().getResponseTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "responseTimeout", provider.getMetadata().getResponseTimeout());
        }
        if (null != provider.getMetadata() && null != provider.getMetadata().getSoTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "soTimeout", provider.getMetadata().getSoTimeout());
        }
        return targetUrl;
    }


}
