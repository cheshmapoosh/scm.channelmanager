package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.plugin.api.exception.ExternalProviderException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProvider;
import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 * @since 2023-08-05
 */
@Component("nabCoreServiceProvider")
public class NabServiceProvider extends AbstractRestExternalServiceProvider {

    @Autowired
    private NabRequestTransformer nabRequestTransformer;
    @Autowired
    private NabResponseTransformer nabResponseTransformer;

    @Override
    protected Object processResponse(HttpResponse<String> response, int statusCode) throws Exception {
        JsonNode node = objectMapper.readTree(response.body());
        if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
            ArrayNode errorsNode = (ArrayNode) node.get("errors");
            if (errorsNode.isArray()) {
                for (JsonNode element : errorsNode) {
                    // Read the data from the array element (assuming they are integers in this example)
                    String errorCode = element.get("id").asText();
                    String errorMessage = element.get("message").asText();
                    throw new ExternalProviderException(externalServiceProvider, errorCode, errorMessage);
                }
            }
        }
        return response.body();
    }

    @Override
    protected HttpResponse<String> prepareResponse(HttpResponse<String> response, Message message, Service service) {
        return super.prepareResponse(response, message, service);
    }

    @Override
    protected Object prepareRequest(Message message, Object requestBody, Service service) {
        return super.prepareRequest(message, requestBody, service);
    }

    @Override
    protected String extractServiceUrl(Service service) {
        try {
            JsonNode componentMetadata = objectMapper.readTree(service.getMetadata());
            return componentMetadata.get("serviceName").asText();
        } catch (JsonProcessingException e) {
            LOGGER.error("error extract serviceUrl for '{}'.", service.getCode(), e);
            return null;
        }
    }

    @Override
    protected String extractServiceHttpMethod(Service service) {
        return "POST";
    }

}
