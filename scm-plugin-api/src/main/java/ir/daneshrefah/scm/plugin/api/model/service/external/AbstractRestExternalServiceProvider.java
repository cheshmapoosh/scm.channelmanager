package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.exception.ProviderUnSuccessfulResponseException;
import ir.daneshrefah.scm.plugin.api.exception.ProviderUnknownException;
import ir.daneshrefah.scm.plugin.api.exception.ProviderUnreachableException;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractRestExternalServiceProvider extends AbstractExternalServiceProvider {

    private static final String PROPERTY_METADATA_ENDPOINT = "endpointUri";
    private static final String PROPERTY_TIMEOUT_CONNECTION = "connectTimeout";
    private static final String PROPERTY_TIMEOUT_RESPONSE = "responseTimeout";
    private String endpointUri;
    private HttpClient httpClient;
    private Integer connectTimeout;
    private Integer responseTimeout;

    @Override
    public boolean initServerConfigs() {
        endpointUri = getMetadataValue(PROPERTY_METADATA_ENDPOINT);
        connectTimeout = getMetadataIntegerValue(PROPERTY_TIMEOUT_CONNECTION);
        connectTimeout = null != connectTimeout ? connectTimeout : 3000;
        responseTimeout = getMetadataIntegerValue(PROPERTY_TIMEOUT_RESPONSE);
        responseTimeout = null != responseTimeout ? responseTimeout : 3000;
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
        return StringUtils.isNotEmpty(endpointUri);
    }

    @Override
    protected JsonNode executeInternal(Message message, Service service, Object requestBody) {
        String serviceUrl = extractUrlByService(service);
        String serviceHttpMethod = extractMethodByService(service);
        String serviceRequestHeaderContentType = extractServiceRequestHeaderContentType(service);
        String targetUrl = endpointUri + serviceUrl;

        HttpRequest.BodyPublisher requestBodyPublisher = null;
        if (null != requestBody)
            requestBodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody.toString());
        else
            requestBodyPublisher = HttpRequest.BodyPublishers.noBody();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMillis(responseTimeout))
                .header(HTTP_HEADER_CONTENT_TYPE, serviceRequestHeaderContentType);

        if ("POST".equalsIgnoreCase(serviceHttpMethod)) {
            builder.POST(requestBodyPublisher);
        } else if ("GET".equalsIgnoreCase(serviceHttpMethod)) {
            builder.GET();
        } else if ("PUT".equalsIgnoreCase(serviceHttpMethod)) {
            builder.PUT(requestBodyPublisher);
        } else if ("DELETE".equalsIgnoreCase(serviceHttpMethod)) {
            builder.DELETE();
        }

        HttpRequest httpRequest = builder.build();

        HttpResponse<String> response = null;

        try {
            LocalDateTime startTime = LocalDateTime.now();
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != HttpConstants.HTTP_STATUS_OK && statusCode != HttpConstants.HTTP_STATUS_NO_CONTENT) {
                JsonNode result = handleUnSuccessfulResponseStatus(response, response.statusCode());
                if (null != result) {
                    return result;
                }
                throw new ProviderUnSuccessfulResponseException(getProvider(), response.statusCode(), response.body());
            }
            return handleSuccessfulResponseStatus(message, service, response);
        } catch (BaseException e) {
            throw e;
        } catch (IOException e) {
            throw new ProviderUnreachableException(getProvider(), e);
        } catch (InterruptedException e) {
            throw new ProviderUnreachableException(getProvider(), e);
        } catch (Exception e) {
            throw new ProviderUnknownException(getProvider(), e);
        }
    }

    protected JsonNode handleSuccessfulResponseStatus(Message message, Service service, HttpResponse<String> response) {
        try {
            return getObjectMapper().readTree(response.body());
        } catch (JsonProcessingException e) {
            return JsonNodeFactory.instance.textNode(e.getMessage());
        }
    }

    protected JsonNode handleUnSuccessfulResponseStatus(HttpResponse<String> response, int statusCode) {
        return null;
    }

    protected abstract String extractMethodByService(Service service);

    protected abstract String extractUrlByService(Service service);

    protected String extractServiceRequestHeaderContentType(Service service) {
        return HTTP_HEADER_CONTENT_TYPE_JSON;
    }

    /*protected Object prepareResponse(HttpResponse<String> response, Message message, Service service) {
        return response.body();
    }

    protected Object prepareRequest(Message message, Object requestBody, Service service) {
        return requestBody;
    }

    protected abstract Object processResponse(HttpResponse<String> response, int statusCode) throws Exception;

    protected Object processError(HttpResponse<String> response, int statusCode) throws Exception {
        return null;
    }


    ;

    protected abstract String extractServiceUrl(Service service);

    protected abstract String extractServiceHttpMethod(Service service);*/

}
