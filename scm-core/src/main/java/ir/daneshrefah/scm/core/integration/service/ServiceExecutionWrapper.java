package ir.daneshrefah.scm.core.integration.service;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-09-05
 */
public class ServiceExecutionWrapper {

    private Service service;
    private JsonSchema requestJsonSchema;
    private JsonSchema responseJsonSchema;
    public ServiceExecutionWrapper(Service service) {
        this.service = service;
        if (StringUtils.isNotEmpty(service.getRequestJsonSchema())) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
            requestJsonSchema = factory.getSchema(service.getRequestJsonSchema());
        }
        if (StringUtils.isNotEmpty(service.getResponseJsonSchema())) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
            responseJsonSchema = factory.getSchema(service.getResponseJsonSchema());
        }
    }

    public Optional<Set<ValidationMessage>> validateRequestSchema(Message message) {
        if (null == requestJsonSchema) {
            return Optional.empty();
        }
        Set<ValidationMessage> errors = requestJsonSchema.validate(message.getPayload());
        if (null == errors || errors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(errors);
    }

}
