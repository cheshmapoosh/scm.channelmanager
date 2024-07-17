package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractRestExternalServiceProviderExecutor extends AbstractCamelExternalServiceProviderExecutor {

    public AbstractRestExternalServiceProviderExecutor(ResourceService resourceService, ObjectMapper objectMapper) {
        super(resourceService, objectMapper);
    }

    @Override
    protected final Map<String, Object> extractRequestHeaders(Message message) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_METHOD, extractHttpMethod(message));
        headers.putAll(extractAdditionalHeaders(message));
        headers.put(HTTP_HEADER_CONTENT_TYPE, extractContentType(message));
        return headers;
    }

    protected Map<String, ?> extractAdditionalHeaders(Message message) {
        return Collections.emptyMap();
    }

    protected String extractHttpMethod(Message message) {
        return HttpMethod.GET.getValue();
    }

    protected String extractContentType(Message message) {
        return HttpContentType.RAW_JSON.getValue();
    }

    @Override
    protected final String extractTargetEndpointUrl(Message message) {
        String targetUrl = extractTargetUrl(message);
        if (null != getProviderModel().getMetadata() && null != getProviderModel().getMetadata().getConnectTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "connectTimeout", getProviderModel().getMetadata().getConnectTimeout());
        }
        if (null != getProviderModel().getMetadata() && null != getProviderModel().getMetadata().getResponseTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "responseTimeout", getProviderModel().getMetadata().getResponseTimeout());
        }
        if (null != getProviderModel().getMetadata() && null != getProviderModel().getMetadata().getSoTimeout()) {
            StringUtils.appendQueryParam(targetUrl, "soTimeout", getProviderModel().getMetadata().getSoTimeout());
        }
        return targetUrl;
    }

    protected abstract String extractTargetUrl(Message message);

}
