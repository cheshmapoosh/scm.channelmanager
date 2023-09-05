package ir.daneshrefah.scm.core.integration.service;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.List;
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
    private List<TransformerExecutionWrapper> requestTransformers;
    private List<TransformerExecutionWrapper> responseTransformers;

    public ServiceExecutionWrapper(Service service) {
        this.service = service;
        if (StringUtils.isNotEmpty(service.getRequestJsonSchema())) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            requestJsonSchema = factory.getSchema(service.getRequestJsonSchema());
        }
        if (StringUtils.isNotEmpty(service.getResponseJsonSchema())) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
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

    public List<TransformerExecutionWrapper> getRequestTransformers() {
        return requestTransformers;
    }

    public void setRequestTransformers(List<TransformerExecutionWrapper> requestTransformers) {
        this.requestTransformers = requestTransformers;
    }

    public List<TransformerExecutionWrapper> getResponseTransformers() {
        return responseTransformers;
    }

    public void setResponseTransformers(List<TransformerExecutionWrapper> responseTransformers) {
        this.responseTransformers = responseTransformers;
    }
}
