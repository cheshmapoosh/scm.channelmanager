package ir.daneshrefah.scm.core.integration.service.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
@Slf4j
@RequiredArgsConstructor
public class ServiceRequestValidationInterceptor extends MessageInterceptor {

    private final ObjectMapper objectMapper;
    private final Map<String, JsonSchema> validators = new HashMap<>();

    @Override
    protected Message internalIntercept(Message message) {
        JsonSchema schema = loadJsonSchemaIfRequired(message.getHeader().getServiceAccess().getService());
        if (null == schema) {
            return message;
        }
        Set<ValidationMessage> errors = schema.validate(message.getPayload());
        if (null == errors || errors.isEmpty()) {
            return message;
        }
        return ErrorHandlerService.getInstance().resolveMessageByValidationMessage(message, errors);
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return true;
    }

    private JsonSchema loadJsonSchemaIfRequired(Service service) {
        if (!validators.containsKey(service.getCode())) {
            JsonSchema schema = null;
            String requestJsonSchema = service.getRequestJsonSchema();
            if (StringUtils.isNotEmpty(requestJsonSchema) && StringUtils.containsNone(requestJsonSchema, "{}")) {
                requestJsonSchema = null;// TODO generateJavaClassSchema(requestJsonSchema);
            }
            if (StringUtils.isNotEmpty(requestJsonSchema)) {
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                schema = factory.getSchema(requestJsonSchema);
            }
            validators.put(service.getCode(), schema);
        }
        return validators.get(service.getCode());
    }

    private String generateJavaClassSchema(String javaClassName) {
        if (StringUtils.isEmpty(javaClassName)) {
            return null;
        }
        try {
            Class<?> modelClass = Class.forName(javaClassName);
//            JsonSchemaConfig config = JsonSchemaConfig.forSchemaVersion(SchemaVersion.DRAFT_7);
            JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
            com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaGen.generateSchema(modelClass);
            return objectMapper.writeValueAsString(schema);
        } catch (Exception e) {
            log.error("error generate schema for class '" + javaClassName + "'", e);
        }
        return null;
    }

}
