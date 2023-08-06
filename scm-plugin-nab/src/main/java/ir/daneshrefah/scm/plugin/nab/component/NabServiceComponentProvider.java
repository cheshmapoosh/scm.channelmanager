package ir.daneshrefah.scm.plugin.nab.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.api.component.AbstractServiceComponentProvider;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.plugin.api.exception.ServiceProviderBusinessException;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.io.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.HTTP_HEADER_CONTENT_TYPE;
import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-05
 */
public class NabServiceComponentProvider extends AbstractServiceComponentProvider<JsonNode> {

    private String baseServerUrl;
    private Integer connectTimeout = 3000;
    private Integer responseTimeout = 3000;

    public NabServiceComponentProvider(ServiceComponentProvider serviceComponentProvider) {
        super(serviceComponentProvider);
    }

    @Override
    public void initServerConfigs() throws Exception {
        metadata = new ObjectMapper().readTree(serviceComponentProvider.getMetadata());
        baseServerUrl = metadata.get("baseUrl").asText();
    }

    @Override
    protected JsonNode transformResponse(Object responseBody, ServiceComponent serviceComponent) {
        if (null == responseBody) {
            return null;
        }
        if (responseBody instanceof String && StringUtils.isEmpty((String) responseBody))
            return null;
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode result = objectMapper.createArrayNode();
        JsonNode responseNode = null;
        if (responseBody instanceof JsonNode)
            responseNode = (JsonNode) responseBody;
        if (responseBody instanceof String) {
            try {
                responseNode = objectMapper.readTree((String) responseBody);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        for (JsonNode element : responseNode.get("result")) {
            // Get the properties from the current element
//            "SRLACC":1399.0,
//            "STATUSX":1,
//            "GENERAL":407,
//            "RANGEID":0,
//            "IBANVALUE":"IR970130100000000000001399",
            String iban = element.get("IBANVALUE").asText();
            String account = element.get("SRLACC").asText();
            String status = element.get("STATUSX").asText();
            String firstName = element.get("FNAME").asText();
            String lastName = element.get("LNAME").asText();
            Integer customerTypeCode = element.get("CUSTOMERTYPE").asInt();
            Integer accountTypeCode = element.get("ACCOUNTYPE").asInt();
            Integer accountStatusCode = element.get("ACCOUNSTATUS").asInt();
//            "TASHILAT":0,
//            "RQID":"15975368"

            // Create a new ObjectNode with modified property names
            ObjectNode modifiedElement = objectMapper.createObjectNode();
            modifiedElement.put("iban", iban);
            modifiedElement.put("account", account);
            modifiedElement.put("status", status);
            modifiedElement.put("firstName", firstName);
            modifiedElement.put("lastname", lastName);
            modifiedElement.put("customerTypeCode", customerTypeCode);
            modifiedElement.put("accountTypeCode", accountTypeCode);
            modifiedElement.put("accountStatusCode", accountStatusCode);

            // Add the modified element to the new array
            result.add(modifiedElement);
        }
        return result;
    }

    @Override
    protected Object executeServiceComponent(Message message, Object requestBody, ServiceComponent serviceComponent) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
        String url = prepareServiceUrl(serviceComponent);
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
                throw new ServiceProviderBusinessException(message.getHeader().getClientCorrelationId(),
                        message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
                        String.valueOf(statusCode), "http status: " + statusCode);
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
                        throw new ServiceProviderBusinessException(message.getHeader().getClientCorrelationId(),
                                message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
                                errorCode, errorMessage);
                    }
                }
            }
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object transformRequest(Message message, ServiceComponent serviceComponent) {
        Object request = message.getPayload();
        AbstractTransformer requestTransformer = TransformerType.JAVA.equals(serviceComponent.getRequestTransformerType()) ?
                ClassLoader.createInstanceOfClass(serviceComponent.getRequestTransformerClass(), AbstractTransformer.class) :
                null;
        if (null != requestTransformer) {
            request = requestTransformer.transform(message, serviceComponent.getRequestMetadata());
//            message.getMessageComponent().setPayload(request);
        }
        return request;
    }

    private String prepareServiceUrl(ServiceComponent serviceComponent) {
        try {
            JsonNode componentMetadata = new ObjectMapper().readTree(serviceComponent.getMetadata());
            String serviceUrl = baseServerUrl + componentMetadata.get("serviceName").asText();

//            String paymentCode = null;
//            if (null != message.getPayload() && null != message.getPayload().get("paymentCode"))
//                paymentCode = message.getPayload().get("paymentCode").asText();
//            if ("123".equals(paymentCode))
//                serviceUrl = "http://10.15.29.81/Service/RCASUSA.IBANINQUIRY";

            return serviceUrl;
        } catch (JsonProcessingException e) {
            LOGGER.error("error extract serviceUrl for '{}'.", serviceComponent.getCode(), e);
            return null;
        }

    }

}
