package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
public abstract class GenericJavaService<S, T> extends AbstractJavaService {

    private Class<S> sourceClass;

    public GenericJavaService(ServiceProducerTemplate serviceComponentExecutor, ObjectMapper objectMapper) {
        super(serviceComponentExecutor, objectMapper);
        TypeToken<S> typeToken = new TypeToken<S>() {};
        this.sourceClass = (Class<S>) typeToken.getRawType();
    }

    @Override
    protected Object internalExecute(Message message, ScmService service, Object payload) {
        S input = deserializeJsonNodeToPOJO(message.getPayload());
        T resultPOJO = execute(input, message.getHeader());
        JsonNode result = serializePOJOToJsonNode(resultPOJO);
        return result;
    }

    private S deserializeJsonNodeToPOJO(JsonNode jsonNode) {
        JavaType javaType = TypeFactory.defaultInstance().constructType(sourceClass);
        try {
            objectMapper.treeToValue(jsonNode, javaType);
        } catch (JsonProcessingException e) {
            return null;
        }
        return null;
//        ObjectMapper mapper = new ObjectMapper();
//        JavaType javaType = TypeFactory.defaultInstance().constructType(clazz);
//        return mapper.treeToValue(jsonNode, javaType);
    }

    private JsonNode serializePOJOToJsonNode(T pojo) {
        return objectMapper.valueToTree(pojo);
    }

    protected abstract T execute(S input, Header header);

}
