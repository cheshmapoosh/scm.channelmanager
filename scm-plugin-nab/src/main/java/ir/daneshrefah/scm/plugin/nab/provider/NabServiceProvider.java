package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProvider;
import org.springframework.stereotype.Component;

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
 * @since 2023-08-05
 */
@Component("nabCoreServiceProvider")
public class NabServiceProvider extends AbstractExternalServiceProvider<JsonNode> {

    private Integer connectTimeout = 3000;
    private Integer responseTimeout = 3000;
    private String baseServerUrl;
    private HttpClient httpClient;


    @Override
    public void initServerConfigs() {
        try {
            metadata = new ObjectMapper().readTree(externalServiceProvider.getMetadata());
            baseServerUrl = metadata.get("baseUrl").asText();
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeout))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object executeServiceComponent(Message message, Object requestBody, Service service) {
        String url = prepareServiceUrl(service);
        HttpRequest.BodyPublisher requestBodyPublisher = null;
        if (null != requestBody)
            requestBodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody.toString());
        else
            requestBodyPublisher = HttpRequest.BodyPublishers.noBody();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(requestBodyPublisher)
                .timeout(Duration.ofMillis(responseTimeout))
                .header(HTTP_HEADER_CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_JSON)
                .build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode == HttpConstants.HTTP_STATUS_BAD_REQUEST) {
//                throw new ServiceProviderBusinessException(message.getHeader().getClientCorrelationId(),
//                        message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
//                        String.valueOf(statusCode), "http status: " + statusCode);
            }
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
                ArrayNode errorsNode = (ArrayNode) node.get("errors");
                if (errorsNode.isArray()) {
                    for (JsonNode element : errorsNode) {
                        // Read the data from the array element (assuming they are integers in this example)
                        String errorCode = element.get("id").asText();
                        String errorMessage = element.get("message").asText();
//                        throw new ServiceProviderBusinessException(message.getHeader().getClientCorrelationId(),
//                                message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
//                                errorCode, errorMessage);
                    }
                }
            }
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String prepareServiceUrl(Service service) {
        try {
            JsonNode componentMetadata = new ObjectMapper().readTree(service.getMetadata());
            String serviceUrl = baseServerUrl + componentMetadata.get("serviceName").asText();

//            String paymentCode = null;
//            if (null != message.getPayload() && null != message.getPayload().get("paymentCode"))
//                paymentCode = message.getPayload().get("paymentCode").asText();
//            if ("123".equals(paymentCode))
//                serviceUrl = "http://10.15.29.81/Service/RCASUSA.IBANINQUIRY";

            return serviceUrl;
        } catch (JsonProcessingException e) {
            LOGGER.error("error extract serviceUrl for '{}'.", service.getCode(), e);
            return null;
        }

    }
}
