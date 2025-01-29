package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import io.swagger.v3.oas.models.tags.Tag;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.RestUrl;
import ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.RestUrlBuilder;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.utils.network.NetworkUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;

import static ir.daneshrefah.scm.utils.constant.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerGenerator {

    private static final ObjectMapper OBJECT_MAPPER;
    private static final String SWAGGER_VERSION = "1.0.1";
    private static SwaggerGenerator SWAGGER_GENERATOR;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        OBJECT_MAPPER.registerModule(javaTimeModule);
    }

    private final ServiceJsonSchemaGenerator serviceJsonSchemaGenerator;
    private final ServiceService serviceService;
    private final Set<String> TAGS = new HashSet<>();
    @Value("${scm.swagger.target-host:#{null}}")
    private String targetHost;

    public static SwaggerGenerator getInstance() {
        return SWAGGER_GENERATOR;
    }

    @PostConstruct
    public void init() {
        SWAGGER_GENERATOR = this;
    }

    public OpenAPI generateOpenAPI(Channel channel, List<TerminalServiceAccess> serviceAccesses,
                                   RestUrlBuilder urlBuilder, String contextPath, Integer port) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title(channel.getTitle()).version(SWAGGER_VERSION));
        //server information
        generateServerInformation(openAPI, channel, contextPath, port);
        //create components
        Components components = new Components();
        //generate security component
        generateSecurityComponent(components);
        openAPI.setComponents(components);
        for (TerminalServiceAccess serviceAccess : serviceAccesses) {
            Service service = serviceService.findServiceByCode(serviceAccess.getService().getCode());
            if (exposedAble(service)) {
                Terminal terminal = serviceAccess.getTerminal();
                RestUrl restUrl = urlBuilder.build(serviceAccess);
                //generate tags
                generateApiTag(openAPI, serviceAccess);
                //Create Operation object
                Operation operation = new Operation();
                //path item
                generatePathItems(openAPI, operation, restUrl);
                //Extract basic information
                generateBasicInformation(service, terminal, operation);
                //path parameter
                generatePathParameters(restUrl.getUrl(), operation);
                //request body
                generateRequestSchema(restUrl, service, operation, components);
                //response body
                generateResponseSchema(service, operation, components);
                //request headers
                generateRequestHeaders(operation);
                //security headers
                generateSecurityHeaders(operation, service);
            }
        }
        return openAPI;
    }

    private void generateApiTag(OpenAPI openAPI, TerminalServiceAccess serviceAccess) {
        Service parent = serviceAccess.getService().getParent();
        if (Objects.nonNull(parent)) {
            List<Tag> tags = openAPI.getTags();
            if (Objects.isNull(tags)) {
                tags = new ArrayList<>();
            }
            String tageName = provideTageName(parent);
            if (TAGS.add(tageName)) {
                Tag tag = new Tag();
                tag.setName(tageName);
                tags.add(tag);
                openAPI.setTags(tags);
            }
        }
    }

    private String provideTageName(Service parent) {
        if (Objects.nonNull(parent)) {
            String alias = parent.getAlias();
            String code = parent.getCode();
            if (Objects.nonNull(alias) && !alias.isBlank()) {
                if (alias.contains("-")) {
                    return alias.replace("-", " ").toUpperCase().replace("/", "");
                } else {
                    return String.join(" ", org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase(alias)).toUpperCase().replace("/", "");
                }
            }
            return code;
        }
        return null;
    }

    private boolean exposedAble(Service service) {
        ServiceStatus status = service.getStatus();
        boolean implanted = true;
        if (service instanceof JavaService javaService) {
            implanted = javaService.isImplemented();
        }
        return Objects.nonNull(status) && !status.equals(ServiceStatus.INTERNAL) && implanted;
    }

    private void generatePathItems(OpenAPI openAPI, Operation operation, RestUrl restUrl) {
        PathItem pathItem;
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
        securityRequirementMap.put("Authorization", apiKeySecurityScheme);
        components.securitySchemes(securityRequirementMap);
    }

    private void generatePathParameters(String url, Operation operation) {
        if (url.contains("{")) {
            List<String> allParameters = StringUtils.findAllParameters(url);
            allParameters.forEach(param -> {
                addPathParameter(operation, param);
            });
        }
    }

    private void generateSecurityHeaders(Operation operation, Service service) {
        Boolean loginAuthentication = service.getCheckAccessFirstAuthentication();
        Boolean transactionAuthentication = service.getCheckAccessSecondAuthentication();
        if (Objects.nonNull(transactionAuthentication) && transactionAuthentication) {
            addHeaderParameter(operation, SCM_PARAMETER_CLAIM_CODE, "Transaction claim", true);
        }
        if (Objects.nonNull(loginAuthentication) && loginAuthentication) {
            SecurityRequirement securityRequirement = new SecurityRequirement();
            securityRequirement.addList("Authorization");
            operation.addSecurityItem(securityRequirement);
        }
    }

    private void generateServerInformation(OpenAPI openAPI, Channel channel, String contextPath, Integer port) {
        Server server = new Server();
        server.description(channel.getTitle());
        server.setUrl(generateBaseUrl(contextPath, port));
        openAPI.setServers(List.of(server));
    }

    private void generateBasicInformation(Service service, Terminal terminal, Operation operation) {
        String tagName = provideTageName(service.getParent());
        operation.setTags(List.of(Objects.nonNull(tagName) ? tagName : "UNDEFINED !"));
        operation.setSummary(service.getCode());
        operation.setDescription(service.getTitle());
        operation.setOperationId(terminal.getCode() + "_" + service.getCode());
    }

    private void generateRequestHeaders(Operation operation) {
        addHeaderParameter(operation, SCM_PARAMETER_TERMINAL, "Terminal", true);
        addHeaderParameter(operation, SCM_PARAMETER_CLIENT_CORRELATION_ID, "Correlation ID Header", false);
        addHeaderParameter(operation, SCM_PARAMETER_CLIENT_TIMESTAMP, "Client timestamp", false);
        addHeaderParameter(operation, SCM_PARAMETER_ACCESS_PARAMETER, "Access parameter", true);
        addHeaderParameter(operation, SCM_PARAMETER_USERNAME, "Username", false);
        addHeaderParameter(operation, SCM_PARAMETER_CLAIM_CODE, "Claim Code", false);
    }

    private void addHeaderParameter(Operation operation, String ref, String description, boolean required) {
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

    private void addPathParameter(Operation operation, String param) {
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
            InetAddress inetAddress = NetworkUtils.findCurrentInet4Address().orElse(Inet4Address.getLocalHost());
            String host = (Objects.nonNull(targetHost)) ? targetHost : inetAddress.getHostAddress();
            return baseUrl.replace(ipAddress, host);
        } catch (Exception e) {
            return baseUrl.replace(ipAddress, "127.0.0.1");
        }
    }

    private void generateResponseSchema(Service service, Operation operation, Components components) {
        String responseJsonSchema = serviceJsonSchemaGenerator.generateResponseSchema(service);
        responseJsonSchema = applyResponseJsonSchemaTemplate(responseJsonSchema);
        if (Objects.nonNull(responseJsonSchema) && !responseJsonSchema.isBlank()) {
            try {
                //parse json
                ApiResponses apiResponses = new ApiResponses();
                String successSchemaName = StringUtils.toCamelCase(service.getCode()) + "RespTO";
                String errorSchemaName = "Error";
                ApiResponse successResponse = createSuccessResponse(successSchemaName);
                ApiResponse errorResponse = createErrorResponse(errorSchemaName);
                apiResponses.addApiResponse("200", successResponse);
                apiResponses.addApiResponse("400", errorResponse);
                operation.setResponses(apiResponses);
                //create schema
                Schema<?> schemaItem = OBJECT_MAPPER.readValue(StringUtils.cleanUpJsonCharacters(responseJsonSchema), Schema.class);
                Schema<?> errorSchemaItem = OBJECT_MAPPER.readValue(StringUtils.cleanUpJsonCharacters(getDefaultErrorSchema()), Schema.class);
                schemaItem.$ref(successSchemaName);
                errorSchemaItem.$ref(errorSchemaName);
                components.addSchemas(successSchemaName, schemaItem);
                components.addSchemas(errorSchemaName, errorSchemaItem);
            } catch (Exception ignore) {
            }
        } else {
            generateDefaultResponseSchema(operation);
        }
    }

    private ApiResponse createErrorResponse(String schemaName) {
        ApiResponse apiResponse = new ApiResponse();
        Content respContent = new Content();
        MediaType respMediaType = new MediaType();
        Schema<Object> objectSchema = new Schema<>();
        objectSchema.set$ref(schemaName);
        respMediaType.schema(objectSchema);
        respContent.put(HTTP_HEADER_CONTENT_TYPE_JSON, respMediaType);
        apiResponse.setContent(respContent);
        apiResponse.setDescription("BAD REQUEST");
        return apiResponse;
    }

    private ApiResponse createSuccessResponse(String schemaName) {
        ApiResponse apiResponse = new ApiResponse();
        Content respContent = new Content();
        MediaType respMediaType = new MediaType();
        Schema<Object> objectSchema = new Schema<>();
        objectSchema.set$ref(schemaName);
        respMediaType.schema(objectSchema);
        respContent.put(HTTP_HEADER_CONTENT_TYPE_JSON, respMediaType);
        apiResponse.setContent(respContent);
        apiResponse.setDescription("SUCCESSFUL");
        return apiResponse;
    }

    private String getDefaultErrorSchema() {
        //language=json
        return """
                {
                  "type": "object",
                  "properties": {
                    "status": {
                      "type": "string",
                      "enum": [
                        "SC_ERROR_VALIDATION",
                        "SC_UNAUTHORIZED",
                        "SC_ACCESS_DENIED",
                        "SC_ERROR_SYSTEM",
                        "SC_ERROR_BUSINESS",
                        "SC_NOT_FOUND",
                        "SC_ERROR_UNREACHABLE_PROVIDER",
                        "SC_ERROR_DATA_INTEGRITY_VIOLATION"
                      ]
                    },
                    "errors": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "source": {
                            "type": "string"
                          },
                          "errorCode": {
                            "type": "string"
                          },
                          "message": {
                            "type": "string"
                          },
                          "messageFa": {
                            "type": "string"
                          },
                          "status": {
                            "type": "string",
                            "enum": [
                              "SC_ERROR_VALIDATION",
                              "SC_UNAUTHORIZED",
                              "SC_ACCESS_DENIED",
                              "SC_ERROR_SYSTEM",
                              "SC_ERROR_BUSINESS",
                              "SC_NOT_FOUND",
                              "SC_ERROR_UNREACHABLE_PROVIDER",
                              "SC_ERROR_DATA_INTEGRITY_VIOLATION"
                            ]
                          }
                        }
                      }
                    }
                  }
                }
                """;
    }

    private String applyResponseJsonSchemaTemplate(String responseJsonSchema) {
        if (StringUtils.isBlank(responseJsonSchema)) {
            return responseJsonSchema;
        }
        String template = """
                {
                  "type": "object",
                  "properties": {
                    "status": {
                      "type": "string",
                      "enum": ["SC_SUCCESS"]
                    },
                    "result": ${x-generated-schema}
                  }
                }
                
                """;
        return template
                .replace("${x-generated-schema}", responseJsonSchema);
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

    private void generateRequestSchema(RestUrl restUrl, Service service, Operation operation, Components components) {
        if (!restUrl.getHttpMethod().equalsIgnoreCase("get")) {
            String requestJsonSchema = serviceJsonSchemaGenerator.generateRequestSchema(service);
            if (Objects.nonNull(requestJsonSchema) && !requestJsonSchema.isBlank()) {
                try {
                    //parse json
                    RequestBody requestBody = new RequestBody();
                    Content reqContent = new Content();
                    MediaType reqMediaType = new MediaType();
                    Schema<Object> objectSchema = new Schema<>();
                    String schemaName = StringUtils.toCamelCase(service.getCode()) + "ReqTO";
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
            } else {
                generateDefaultRequestSchema(operation);
            }
        }
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


    public String cleanupSwaggerJson(String swaggerJson) {
        swaggerJson = swaggerJson.replace(SecurityScheme.Type.APIKEY.name(), SecurityScheme.Type.APIKEY.toString());
        swaggerJson = swaggerJson.replace(SecurityScheme.In.HEADER.name(), SecurityScheme.In.HEADER.toString());
        swaggerJson = swaggerJson.replace(SecurityScheme.Type.HTTP.name(), SecurityScheme.Type.HTTP.toString());
        swaggerJson = swaggerJson.replace(",\"exampleSetFlag\":false", "");
        swaggerJson = swaggerJson.replace(",\"exampleSetFlag\":true", "");
        return swaggerJson;
    }
}
