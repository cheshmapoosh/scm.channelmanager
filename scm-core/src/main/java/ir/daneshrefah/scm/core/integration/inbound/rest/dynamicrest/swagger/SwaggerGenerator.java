package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;
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
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.RestUrl;
import ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.RestUrlBuilder;
import ir.daneshrefah.scm.core.integration.service.JavaServiceFinder;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Component
public class SwaggerGenerator {

    @Value("${scm.swagger.server-host:#{null}}")
    private String serverHost;
    private static final ObjectMapper OBJECT_MAPPER;
    private static final SwaggerGenerator SWAGGER_GENERATOR = new SwaggerGenerator();
    private static final String SWAGGER_VERSION = "1.0.0";

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS,false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS,false);
    }
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
        //generate security component
        generateSecurityComponent(components);
        openAPI.setComponents(components);
        for (TerminalServiceAccess serviceAccess : serviceAccesses) {
            RestUrl restUrl = urlBuilder.build(serviceAccess);
            //Create Operation object
            Operation operation = new Operation();
            //path item
            generatePathItems(openAPI,operation,restUrl);
            //Extract basic information
            generateBasicInformation(channel,serviceAccess, operation, restUrl);
            //path parameter
            generatePathParameters(restUrl.getUrl(),operation);
            //request body
            generateRequestSchema(restUrl,serviceAccess, operation, components);
            //response body
            generateResponseSchema(serviceAccess, operation, components);
            //request headers
            generateRequestHeaders(operation);
            //security headers
            generateSecurityHeaders(operation,serviceAccess);
        }
        return openAPI;
    }

    private void generatePathItems(OpenAPI openAPI, Operation operation,RestUrl restUrl) {
        PathItem pathItem ;
        String url = "/" + restUrl.getUrl();
        if (null != openAPI.getPaths() && null != openAPI.getPaths().get(url)) {
            pathItem = openAPI.getPaths().get(url);
        } else {
            pathItem = new PathItem();
            openAPI.path(url, pathItem);
        }
        pathItem.operation(PathItem.HttpMethod.valueOf(restUrl.getHttpMethod().toUpperCase()), operation);
    }

    private void generateSecurityComponent(Components components) {
        //bearer component
        Map<String, SecurityScheme> securityRequirementMap = new HashMap<>();
        //api key header
        SecurityScheme apiKeySecurityScheme = new SecurityScheme();
        apiKeySecurityScheme.setType(SecurityScheme.Type.APIKEY);
        apiKeySecurityScheme.in(SecurityScheme.In.HEADER);
        apiKeySecurityScheme.setName(SCM_PARAMETER_AUTHORIZATION);
        //create security map
        securityRequirementMap.put("Authorization",apiKeySecurityScheme);
        components.securitySchemes(securityRequirementMap);
    }

    private void generatePathParameters(String url, Operation operation) {
        if (url.contains("{") ){
            List<String> allParameters = StringUtils.findAllParameters(url);
            allParameters.forEach(param->{
                addPathParameter(operation,param);
            });
        }
    }


    private void generateSecurityHeaders(Operation operation, TerminalServiceAccess serviceAccess) {
        Boolean loginAuthentication = serviceAccess.getService().getCheckAccessFirstAuthentication();
        Boolean transactionAuthentication = serviceAccess.getService().getCheckAccessSecondAuthentication();
        if (Objects.nonNull(transactionAuthentication) && transactionAuthentication){
            addHeaderParameter(operation,SCM_PARAMETER_CLAIM_CODE,"Transaction claim",true);
        }
        if (Objects.nonNull(loginAuthentication) && loginAuthentication){
            SecurityRequirement securityRequirement = new SecurityRequirement();
            securityRequirement.addList("Authorization");
            operation.addSecurityItem(securityRequirement);
        }
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
        operation.setOperationId(serviceAccess.getTerminal().getCode() + "_" + serviceAccess.getService().getCode());
    }

    private void generateRequestHeaders(Operation operation) {
        addHeaderParameter(operation,SCM_PARAMETER_TERMINAL,"Terminal",true);
        addHeaderParameter(operation,SCM_PARAMETER_CLIENT_CORRELATION_ID,"Correlation ID Header",false);
        addHeaderParameter(operation,SCM_PARAMETER_CLIENT_TIMESTAMP,"Client timestamp",false);
        addHeaderParameter(operation,SCM_PARAMETER_ACCESS_PARAMETER,"Access parameter",true);
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

    private void addPathParameter(Operation operation,String param){
        Parameter parameter = new PathParameter();
        parameter.setIn("path");
        parameter.setName(param);
        Schema<String> schema = new Schema<>();
        schema.setType("string");
        schema.setName(param);
        parameter.setSchema(schema);
        operation.addParametersItem(parameter);
    }

    private String generateBaseUrl(String contextPath, Integer port) {
        final String ipAddress = "{ip-address}";
        String baseUrl = "http://" + ipAddress + ":" + port + contextPath;
        try {
            String host = Objects.nonNull(serverHost) ? serverHost : InetAddress.getLocalHost().getHostAddress();
            return baseUrl.replace(ipAddress, host);
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
                String schemaName = StringUtils.toCamelCase(serviceAccess.getService().getCode()) + "RespTO";
                objectSchema.set$ref(schemaName);
                respMediaType.schema(objectSchema);
                respContent.put(HTTP_HEADER_CONTENT_TYPE_JSON, respMediaType);
                apiResponse.setContent(respContent);
                apiResponse.setDescription("successful");
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
        respContent.put(HTTP_HEADER_CONTENT_TYPE_JSON, value);
        apiResponse.setContent(respContent);
        apiResponse.setDescription("successful");
        apiResponses.addApiResponse("200", apiResponse);
        apiResponses.setExtensions(null);
        operation.setResponses(apiResponses);
    }

    private void generateRequestSchema(RestUrl restUrl,TerminalServiceAccess serviceAccess, Operation operation, Components components) {
        if (!restUrl.getHttpMethod().equalsIgnoreCase("get")){
            Service service = serviceAccess.getService();
            String requestJsonSchema = service.getRequestJsonSchema();
            ServiceImplementationType implementationType = service.getImplementationType();
            if (Objects.nonNull(implementationType) && ServiceImplementationType.JAVA.equals(implementationType)){
                requestJsonSchema = generateJavaServiceRequestSchema(service);
            }
            if (Objects.nonNull(requestJsonSchema) && !requestJsonSchema.isBlank()) {
                try {
                    //parse json
                    RequestBody requestBody = new RequestBody();
                    Content reqContent = new Content();
                    MediaType reqMediaType = new MediaType();
                    Schema<Object> objectSchema = new Schema<>();
                    String schemaName = StringUtils.toCamelCase(serviceAccess.getService().getCode())+ "ReqTO";
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
            }else {
                generateDefaultRequestSchema(operation);
            }
        }
    }

    private String generateJavaServiceRequestSchema(Service service) {
        try {
            final String ignoreType = "Message";
            final String basePackage = "ir.daneshrefah";
            JavaService javaService = (JavaService) service;
            JavaServiceFinder.MethodInfo methodInfo = JavaServiceFinder.findJavaServiceMethodInfo(javaService);
            Class<?>[] parameterTypes = methodInfo.getMethod().getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                if (parameterType.toString().contains(basePackage) && !parameterType.toString().contains(ignoreType)){
                    Class<?> modelClass = Class.forName(parameterType.getName());
                    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(OBJECT_MAPPER);
                    JsonSchema schema = schemaGen.generateSchema(modelClass);
                    return OBJECT_MAPPER.writeValueAsString(schema);
                }
            }
        }catch (Exception ignore){}
        return null;
    }

    private void generateDefaultRequestSchema(Operation operation) {
        RequestBody requestBody = new RequestBody();
        Content reqContent = new Content();
        MediaType reqMediaType = new MediaType();
        Schema<Object> objectSchema = new Schema<>();
        objectSchema.setType("object");
        reqMediaType.schema(objectSchema);
        reqContent.addMediaType(HTTP_HEADER_CONTENT_TYPE_JSON, reqMediaType);
        requestBody.setContent(reqContent);
        operation.setRequestBody(requestBody);
    }


    public String cleanupSwaggerJson(String swaggerJson)  {
        swaggerJson  = swaggerJson.replace(SecurityScheme.Type.APIKEY.name(), SecurityScheme.Type.APIKEY.toString());
        swaggerJson = swaggerJson.replace(SecurityScheme.In.HEADER.name(),SecurityScheme.In.HEADER.toString());
        swaggerJson = swaggerJson.replace(SecurityScheme.Type.HTTP.name(),SecurityScheme.Type.HTTP.toString());
        swaggerJson = swaggerJson.replace(",\"exampleSetFlag\":false","");
        swaggerJson = swaggerJson.replace(",\"exampleSetFlag\":true","");
        return swaggerJson;
    }
}
