package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import static ir.daneshrefah.scm.utils.constant.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-19
 */
public class SwaggerGenerator {

    /*
       //TODO FOR TEST RESULT
       1.please get swagger json from : http://localhost:8082/ib4dev/api-docs/swagger.json
       2.paste the json on online viewer : https://editor-next.swagger.io/
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final SwaggerGenerator SWAGGER_GENERATOR = new SwaggerGenerator();
    private static final String SWAGGER_VERSION = "1.0.0";

    private SwaggerGenerator() {
    }

    public static SwaggerGenerator getInstance() {
        return SWAGGER_GENERATOR;
    }



    public OpenAPI generateOpenAPI(Channel channel, List<TerminalServiceAccess> serviceAccesses,
                                   RestUrlBuilder urlBuilder, String contextPath, Integer port) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title(channel.getTitle()).version(SWAGGER_VERSION));
        //server information
        generateServerInformation(openAPI,channel, contextPath, port);
        //create components
        Components components = new Components();
        openAPI.setComponents(components);
        //generate component headers

        for (TerminalServiceAccess serviceAccess : serviceAccesses) {
            RestUrl restUrl = urlBuilder.build(serviceAccess);
            PathItem pathItem = null;
            if (null != openAPI.getPaths() && null != openAPI.getPaths().get(restUrl.getUrl())) {
                pathItem = openAPI.getPaths().get(restUrl.getUrl());
            } else {
                pathItem = new PathItem();
                openAPI.path("/" + restUrl.getUrl(), pathItem);
            }

            // Create Operation object
            Operation operation = new Operation();
            // Extract basic information
            generateBasicInformation(channel,serviceAccess, operation, restUrl);
            //request body
            generateRequestSchema(serviceAccess, operation, components);
            //response body
            generateResponseSchema(serviceAccess, operation, components);
            //request headers
            generateRequestHeaders(operation);
            pathItem.operation(PathItem.HttpMethod.valueOf(restUrl.getHttpMethod().toUpperCase()), operation);

        }
        return openAPI;
    }

    private void generateServerInformation(OpenAPI openAPI,Channel channel, String contextPath, Integer port) {
        Server server = new Server();
        server.description(channel.getTitle());
        server.setUrl(generateBaseUrl(contextPath, port));
        openAPI.setServers(List.of(server));
    }

    private void generateBasicInformation(Channel channel,TerminalServiceAccess serviceAccess, Operation operation, RestUrl restUrl) {
        operation.setTags(List.of(channel.getTitle()));
        operation.setSummary(serviceAccess.getService().getCode());
        operation.setDescription(serviceAccess.getService().getTitle());
        operation.setOperationId(restUrl.getUrl());
    }

    private void generateRequestHeaders(Operation operation) {
        addHeaderParameter(operation,SCM_PARAMETER_TERMINAL,"Terminal",true);
        addHeaderParameter(operation,SCM_PARAMETER_CLIENT_CORRELATION_ID,"Correlation ID Header",false);
        addHeaderParameter(operation,SCM_PARAMETER_CLIENT_TIMESTAMP,"Client timestamp",false);
        addHeaderParameter(operation,SCM_PARAMETER_ACCESS_PARAMETER,"Access parameter",true);
        addHeaderParameter(operation,SCM_PARAMETER_AUTHENTICATION,"Authentication parameter",true);
    }

    private void addHeaderParameter(Operation operation,String ref,String description,boolean required){
        Parameter parameter = new HeaderParameter();
        parameter.setIn("header");
        parameter.setName(ref);
        parameter.setDescription(description);
        Schema<String> schema = new Schema<>();
        schema.setType("string");
        schema.setName(ref);
        parameter.setSchema(schema);
        parameter.setRequired(required);
        operation.addParametersItem(parameter);
    }

    private String generateBaseUrl(String contextPath, Integer port) {
        final String ipAddress = "{ip-address}";
        String baseUrl = "http://" + ipAddress + ":" + port + contextPath;
        try {
            return baseUrl.replace(ipAddress, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            return baseUrl.replace(ipAddress, "0.0.0.0");
        }
    }

    private void generateResponseSchema(TerminalServiceAccess serviceAccess, Operation operation, Components components) {
        String responseJsonSchema = serviceAccess.getService().getResponseJsonSchema();
        if (Objects.nonNull(responseJsonSchema) && !responseJsonSchema.isBlank()) {
            try {
                //parse json
                ApiResponses apiResponses = new ApiResponses();
                ApiResponse apiResponse = new ApiResponse();
                Content respContent = new Content();
                MediaType respMediaType = new MediaType();
                Schema<Object> objectSchema = new Schema<>();
                String schemaName = CaseUtils.toCamelCase(serviceAccess.getService().getCode(), true, '_') + "RespTO";
                objectSchema.set$ref(schemaName);
                respMediaType.schema(objectSchema);
                respContent.put(HTTP_HEADER_CONTENT_TYPE_JSON, respMediaType);
                apiResponse.setContent(respContent);
                apiResponses.addApiResponse("200", apiResponse);
                operation.setResponses(apiResponses);
                //create schema
                Schema<?> schemaItem = OBJECT_MAPPER.readValue(StringUtils.cleanUpJsonCharacters(responseJsonSchema), Schema.class);
                components.addSchemas(schemaName, schemaItem);
            } catch (Exception ignore) {
            }
        } else {
            generateDefaultResponseSchema(operation);
        }
    }

    private void generateDefaultResponseSchema(Operation operation) {
        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        Content respContent = new Content();
        MediaType value = new MediaType();
        Schema<Object> objectSchema = new Schema<>();
        objectSchema.setType("object");
        value.schema(objectSchema);
        respContent.put("*/*", value);
        apiResponse.setContent(respContent);
        apiResponses.addApiResponse("200", apiResponse);
        apiResponses.setExtensions(null);
        operation.setResponses(apiResponses);
    }

    private void generateRequestSchema(TerminalServiceAccess serviceAccess, Operation operation, Components components) {
        String requestJsonSchema = serviceAccess.getService().getRequestJsonSchema();
        if (Objects.nonNull(requestJsonSchema) && !requestJsonSchema.isBlank()) {
            try {
                //parse json
                RequestBody requestBody = new RequestBody();
                Content reqContent = new Content();
                MediaType reqMediaType = new MediaType();
                Schema<Object> objectSchema = new Schema<>();
                String schemaName = CaseUtils.toCamelCase(serviceAccess.getService().getCode(), true, '_') + "ReqTO";
                objectSchema.set$ref(schemaName);
                reqMediaType.schema(objectSchema);
                reqContent.addMediaType(HTTP_HEADER_CONTENT_TYPE_JSON, reqMediaType);
                requestBody.setContent(reqContent);
                operation.setRequestBody(requestBody);
                //create schema
                Schema<?> schemaItem = OBJECT_MAPPER.readValue(StringUtils.cleanUpJsonCharacters(requestJsonSchema), Schema.class);
                components.addSchemas(schemaName, schemaItem);
            } catch (Exception ignore) {
            }
        }
    }

}
