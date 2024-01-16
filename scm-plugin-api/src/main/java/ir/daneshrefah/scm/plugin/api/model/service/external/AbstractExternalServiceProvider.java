package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Getter
public abstract class AbstractExternalServiceProvider {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExternalServiceProvider provider;
    private JsonNode metadata;

    public boolean initServerConfigs(ExternalServiceProvider provider) {
        this.provider = provider;
        try {
            if (StringUtils.isNotEmpty(provider.getMetadata())) {
                metadata = objectMapper.readTree(provider.getMetadata());
            } else {
                metadata = objectMapper.createObjectNode();
            }
            return initServerConfigs();
        } catch (JsonProcessingException e) {
            LOGGER.error("error on init provider '{}' metadata.", provider.getCode());
            return false;
        }
    }

    protected abstract boolean initServerConfigs();

    public Object execute(Message message, Service service, Object requestPayload) {
        requestPayload = transformRequest(message, requestPayload);
        Object response = executeInternal(message, service, requestPayload);
        return transformResponse(message, response);
    }

    private Object transformRequest(Message message, Object requestBody) {
        List<AbstractTransformer> requestTransformers = prepareRequestTransformers();
        for (Iterator<AbstractTransformer> iterator = requestTransformers.iterator(); iterator.hasNext(); ) {
            AbstractTransformer transformer = iterator.next();
            requestBody = transformer.transform(requestBody, message, message.getHeader().getService().getTerminalServiceAccess().getService().getMetadata());
        }
        return requestBody;
    }

    private Object transformResponse(Message message, Object response) {
        List<AbstractTransformer> responseTransformers = prepareRequestTransformers();
        for (Iterator<AbstractTransformer> iterator = responseTransformers.iterator(); iterator.hasNext(); ) {
            AbstractTransformer transformer = iterator.next();
            response = transformer.transform(response, message, null);
        }
        return response;
    }

    protected List<AbstractTransformer> prepareRequestTransformers() {
        return Collections.emptyList();
    }

    protected List<AbstractTransformer> prepareResponseTransformers() {
        return Collections.emptyList();
    }

    protected abstract Object executeInternal(Message message, Service service, Object requestBody);

    protected String getMetadataValue(String key) {
        if (null == metadata || !metadata.has(key)) {
            return null;
        }
        return metadata.get(key).asText();
    }

    protected Integer getMetadataIntegerValue(String key) {
        if (null == metadata || !metadata.has(key)) {
            return null;
        }
        return metadata.get(key).asInt();
    }

}
