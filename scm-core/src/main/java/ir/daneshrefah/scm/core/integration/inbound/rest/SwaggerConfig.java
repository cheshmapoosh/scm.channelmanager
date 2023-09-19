package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.lang.reflect.Field;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-09-18
 */
@Configuration
public class SwaggerConfig {

    @Value("8082")
    private int serverPort;
    @Autowired
    private ServiceService service;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public JsonNode openApiGenerator() {

        OpenAPI openAPI = new OpenAPI();

        openAPI.setServers(getServerAddress().stream().map(s -> {
            return new Server().url("http://" + s + ":" + serverPort);
        }).collect(Collectors.toList()));

        Info info = new Info()
                .title("Sample API")
                .description("This is a sample API documentation.")
                .version("1.0.0");
        openAPI.info(info);

        Schema<?> requestSchema = getSchema("requestSchema");
        Schema<?> responseSchema = getSchema("responseSchema");
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        pathItem   //TODO Check method
                .post(new Operation()
                        .requestBody(new RequestBody()
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(requestSchema)
                                        )
                                )
                        )
                        .parameters(generateHeaderParameters(HttpConstants.class))
                        .responses(createApiResponse("200","Success Response",responseSchema)
                        )
                );

        paths.addPathItem("/api/nickNameModifications", pathItem); //TODO Read this from DB
        openAPI.paths(paths);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper.convertValue(openAPI, ObjectNode.class);

    }

    public ApiResponses createApiResponse(String statusCode, String description, Schema<?> schema) {
        return new ApiResponses()
                .addApiResponse(statusCode, new ApiResponse()
                        .description(description)
                        .content(new Content()
                                .addMediaType("application/json", new MediaType()
                                        .schema(schema)
                                )
                        )
                );
    }
    public  List<Parameter> generateHeaderParameters(Class<?> constantsClass) {
        List<Parameter> headerParameters = new ArrayList<>();

        Field[] fields = constantsClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == String.class) {
                try {
                    String fieldName = (String) field.get(null);
                    Parameter parameter = new Parameter()
                            .in(String.valueOf(ParameterIn.HEADER))
                            .name(fieldName)
                            .description( fieldName)
                            .required(false)
                            .schema(new StringSchema());
                    headerParameters.add(parameter);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage());
                }
            }
        }

        return headerParameters;
    }

    private List<String> getServerAddress() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                for (InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses())
                    if (interfaceAddress.getAddress().isSiteLocalAddress())
                        addresses.add(interfaceAddress.getAddress().getHostAddress());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private Schema<?> getSchema(String fieldName){

        ObjectMapper objectMapper = new ObjectMapper();
        Iterable<Service> serviceEntities = service.findServiceList();

        for (Service service : serviceEntities) {
            try {
                String schemaContent =
                        (fieldName.equals("requestSchema") && service.getRequestJsonSchema() != null)
                                ? service.getRequestJsonSchema()
                                : (fieldName.equals("responseSchema") && service.getResponseJsonSchema() != null)
                                ? service.getResponseJsonSchema()
                                : null;

                if (schemaContent != null) {
                    return objectMapper.readValue(schemaContent, Schema.class);
                }

            } catch (JsonProcessingException exception) {
                logger.error("Error while parsing JSON schema: " + exception.getMessage(), exception);
                return createEmptySchema();
            } catch (IllegalArgumentException exception) {
                logger.error("IllegalArgumentException occurred: " + exception.getMessage(), exception);
                return createEmptySchema();
            } catch (NullPointerException exception) {
                logger.error("NullPointerException occurred: " + exception.getMessage(), exception);
                return createEmptySchema();
            }
        }

        return createEmptySchema();
    }

    private Schema<?> createEmptySchema() {
        return new Schema<>();
    }
}