package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractRestExternalServiceProvider extends AbstractCamelExternalServiceProviderExecutor {

    private static final String DEFAULT_HTTP_METHOD = "GET";

    public AbstractRestExternalServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext, ObjectMapper objectMapper) {
        super(producerTemplate, camelContext, objectMapper);
    }

    @Override
    protected final Map<String, Object> obtainRequestHeaders(Message message) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_METHOD, extractHttpMethod(message));
        headers.putAll(extractAdditionalHeaders(message));
        headers.put("Content-Type", "application/json");
        return headers;
    }

    protected Map<String, ?> extractAdditionalHeaders(Message message) {
        return Collections.emptyMap();
    }

    protected String extractHttpMethod(Message message) {
        return DEFAULT_HTTP_METHOD;
    }

    @Override
    protected final String extractTargetUrl(Message message) {
        String targetUrl = prepareTargetUrl(message);
        if (null != getProvider().getMetadata() && null != getProvider().getMetadata().getConnectTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "connectTimeout", getProvider().getMetadata().getConnectTimeout());
        }
        if (null != getProvider().getMetadata() && null != getProvider().getMetadata().getResponseTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "responseTimeout", getProvider().getMetadata().getResponseTimeout());
        }
        if (null != getProvider().getMetadata() && null != getProvider().getMetadata().getSoTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "soTimeout", getProvider().getMetadata().getSoTimeout());
        }
        return targetUrl;
    }

    protected abstract String prepareTargetUrl(Message message);

}
