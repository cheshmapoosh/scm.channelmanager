package ir.daneshrefah.scm.plugin.nab.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.plugin.api.component.AbstractEndpoint;
import ir.daneshrefah.scm.plugin.api.component.AbstractProducer;
import ir.daneshrefah.scm.plugin.api.exception.ServiceProviderBusinessException;
import ir.daneshrefah.scm.plugin.api.exception.ServiceProviderUnreachableException;
import ir.daneshrefah.scm.plugin.api.model.message.Message;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class NabProducer extends AbstractProducer {

    public NabProducer(AbstractEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public Object internalProcess(Message message) throws Exception {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build();
        String serviceUrl = prepareServiceUrl(message);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl))
                .POST(HttpRequest.BodyPublishers.ofString(message.getMessageComponent().getPayload().toString()))
                .header(HTTP_HEADER_CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_JSON)
                .build();

        HttpResponse<String> response = null;
//        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
                ArrayNode errorsNode = (ArrayNode) node.get("errors");
                if (errorsNode.isArray()) {
                    for (JsonNode element : errorsNode) {
                        // Read the data from the array element (assuming they are integers in this example)
                        String errorCode = element.get("id").asText();
                        String errorMessage = element.get("message").asText();
                        throw new ServiceProviderBusinessException(message.getHeader().getCorrelationId(),
                                message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
                                errorCode, errorMessage);
                    }
                }
            }
//        } catch (Exception e) {
//            throw e;
//        }
//        } catch (ExecutionException e) {
//            if (e.getCause() instanceof IOException) {
//                System.out.println("Error: " + e.getCause().getMessage());
//            } else {
//                e.printStackTrace();
//            }
//        } catch (ConnectException e) {
//            LOGGER.error("error on message '{}' with address '{}'", message.getHeader().getCorrelationId(),
//                    serviceUrl, e);
//            throw new ServiceProviderUnreachableException(message.getHeader().getCorrelationId(),
//                    message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(), "");
//        } catch (InterruptedException e) {
////            System.out.println("HTTP request interrupted");
//        } catch (Exception e) {
////            e.printStackTrace();
//            throw e;
//        }
        return response.body();
//        return "Test Nab ";
    }

    private String prepareServiceUrl(Message message) {
        message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getMetadata();
        message.getMessageComponent().getServiceComponent().getMetadata();
        String baseUrl = "";
        String serviceName = "";
        String paymentCode = message.getPayload().get("paymentCode").asText();
        if ("123".equals(paymentCode))
            return "http://10.15.29.81/Service/RCASUSA.IBANINQUIRY";
        else
            return "http://10.15.29.80/Service/RCASUSA.IBANINQUIRY";

    }

}
