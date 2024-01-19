package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

import java.util.Iterator;
import java.util.List;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CORRELATION_ID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-19
 */
public class SwaggerGenerator {

    public static OpenAPI generateOpenAPI(Channel channel, List<TerminalServiceAccess> serviceAccesses,
                                          RestUrlBuilder urlBuilder) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info().title(channel.getTitle()).version("1.0.0"));
        Components components = new Components();
        Header myHeader = new Header().$ref("correlationId").description("Correlation ID Header");
        components.addHeaders(SCM_PARAMETER_CORRELATION_ID, myHeader);
        openAPI.setComponents(components);
        for (Iterator<TerminalServiceAccess> iterator = serviceAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceAccess serviceAccess = iterator.next();
            RestUrl restUrl = urlBuilder.build(serviceAccess);
            PathItem pathItem = null;
            if (null != openAPI.getPaths() && null != openAPI.getPaths().get(restUrl.getUrl())) {
                pathItem = openAPI.getPaths().get(restUrl.getUrl());
            } else {
                pathItem = new PathItem();
                openAPI.path(restUrl.getUrl(), pathItem);
            }

            // Create Operation object
            Operation operation = new Operation();

            // Extract basic information
            operation.setSummary(serviceAccess.getService().getCode());
            operation.setDescription(serviceAccess.getService().getTitle());
            operation.setOperationId(restUrl.getUrl());
            operation.addParametersItem(new HeaderParameter().$ref("#/components/headers/correlationId"));
//                operation.setTags(extractTags(method));

                /*// Extract parameters (ensure proper validation and sanitization)
                List<Parameter> parameters = extractParameters(method);
                for (Parameter parameter : parameters) {
                    // Validate parameter values and apply necessary sanitization to prevent potential vulnerabilities
                    // ... (Implement validation and sanitization logic here)
                    operation.addParametersItem(parameter);
                }

                // Extract responses
                List<ApiResponse> responses = extractResponses(method);
                for (ApiResponse response : responses) {
                    operation.addResponsesItem(response);
                }

                // Extract security requirements (if applicable)
                extractSecurityRequirements(method, operation);*/


            pathItem.operation(PathItem.HttpMethod.valueOf(restUrl.getHttpMethod().toUpperCase()), operation);
        }
        return openAPI;
    }

}
