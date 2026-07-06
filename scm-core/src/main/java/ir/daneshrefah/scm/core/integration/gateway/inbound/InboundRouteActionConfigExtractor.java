package ir.daneshrefah.scm.core.integration.gateway.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class InboundRouteActionConfigExtractor {
    private static final String INBOUND_ACTION = "inboundAction";

    private final ObjectMapper objectMapper;

    public InboundRouteActionConfigExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public InboundRouteActionConfig extract(InboundChannelServiceDefinition inboundDefinition) {
        Definition definition = inboundDefinition == null ? null : inboundDefinition.getDefinition();
        if (definition == null || StringUtils.isBlank(definition.getDetails())) {
            return InboundRouteActionConfig.absent();
        }

        JsonNode details = parseDetails(inboundDefinition, definition);
        if (!details.isObject()) {
            throw configurationException(inboundDefinition, definition,
                    "Definition.details must be a JSON object");
        }
        JsonNode inboundAction = details.get(INBOUND_ACTION);
        if (inboundAction == null || inboundAction.isNull()) {
            return InboundRouteActionConfig.absent();
        }
        if (!inboundAction.isTextual()) {
            throw configurationException(inboundDefinition, definition,
                    "field=inboundAction must be a string");
        }
        return new InboundRouteActionConfig(true, StringUtils.trimToNull(inboundAction.asText()));
    }

    private JsonNode parseDetails(
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition
    ) {
        try {
            return objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw configurationException(inboundDefinition, definition,
                    "Definition.details must contain valid JSON", exception);
        }
    }

    private IllegalStateException configurationException(
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            String message
    ) {
        return configurationException(inboundDefinition, definition, message, null);
    }

    private IllegalStateException configurationException(
            InboundChannelServiceDefinition inboundDefinition,
            Definition definition,
            String message,
            Throwable cause
    ) {
        String text = "Invalid inbound route action configuration for channelServiceDefinitionId="
                + value(inboundDefinition == null ? null : inboundDefinition.getId())
                + ", definitionId=" + value(definition == null ? null : definition.getId())
                + ", definitionName=" + value(definition == null ? null : definition.getName())
                + ": " + message;
        return cause == null ? new IllegalStateException(text) : new IllegalStateException(text, cause);
    }

    private String value(Object value) {
        return value == null ? "<null>" : String.valueOf(value);
    }
}
