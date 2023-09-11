package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.exception.ExternalProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.HTTP_HEADER_CONTENT_TYPE;
import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractRestExternalServiceProvider extends AbstractExternalServiceProvider<JsonNode> {

    private String endpointUri;
    private HttpClient httpClient;
    private Integer connectTimeout = 3000;
    private Integer responseTimeout = 3000;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initServerConfigs() {
        try {
            metadata = objectMapper.readTree(externalServiceProvider.getMetadata());
            endpointUri = metadata.get("endpointUri").asText();
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeout))
                    .build();
        } catch (Exception e) {
            LOGGER.error("error on init externalServiceProvider: " + externalServiceProvider.getCode(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object executeInternal(Message message, Object requestBody, Service service) {
        String serviceUrl = extractServiceUrl(service);
        String serviceHttpMethod = extractServiceUrl(service);
        String serviceRequestHeaderContentType = extractServiceRequestHeaderContentType(service);
        String targetUrl = endpointUri + serviceUrl;
        requestBody = prepareRequest(message, requestBody, service);
        HttpRequest.BodyPublisher requestBodyPublisher = null;
        if (null != requestBody)
            requestBodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody.toString());
        else
            requestBodyPublisher = HttpRequest.BodyPublishers.noBody();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMillis(responseTimeout))
                .header(HTTP_HEADER_CONTENT_TYPE, serviceRequestHeaderContentType);

        builder.POST(requestBodyPublisher); //TODO check by serviceHttpMethod

        HttpRequest httpRequest = builder.build();

        HttpResponse<String> response = null;

        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != HttpConstants.HTTP_STATUS_OK && statusCode != HttpConstants.HTTP_STATUS_NO_CONTENT) {
                Object result = processError(response, response.statusCode());
                if (null != result) {
                    return result;
                }
                throw new ExternalProviderException(externalServiceProvider, String.valueOf(response.statusCode()), response.body());
//                throw new ServiceProviderBusinessException(message.getHeader().getClientCorrelationId(),
//                        message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
//                        String.valueOf(statusCode), "http status: " + statusCode);
            }
            response = prepareResponse(response, message, service);
            return processResponse(response, response.statusCode());
        } catch (BaseException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected HttpResponse<String> prepareResponse(HttpResponse<String> response, Message message, Service service) {
        return response;
    }

    protected Object prepareRequest(Message message, Object requestBody, Service service) {
        return requestBody;
    }

    protected abstract Object processResponse(HttpResponse<String> response, int statusCode) throws Exception;
    protected Object processError(HttpResponse<String> response, int statusCode) throws Exception {
        return null;
    }

    protected String extractServiceRequestHeaderContentType(Service service) {
        return HTTP_HEADER_CONTENT_TYPE_JSON;
    };

    protected abstract String extractServiceUrl(Service service);

    protected abstract String extractServiceHttpMethod(Service service);

}
