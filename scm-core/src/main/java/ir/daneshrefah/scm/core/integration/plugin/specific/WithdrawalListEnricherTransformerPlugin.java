package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalListEnricherTransformerPlugin implements PluginHandler {

    private final ObjectMapper objectMapper;

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        PluginPhase phase = pluginDetail.getPhase();
        if (!PluginPhase.AFTER.equals(phase)) {
            throw new IllegalArgumentException("Plugin phase " + phase + " is not supported on 'accountListEnricherPlugin'");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        String jsonBody = String.valueOf(exchange.getIn().getBody());
        JsonNode body = objectMapper.readTree(jsonBody);
        if ((body instanceof ArrayNode sourceArray)) {
            ArrayNode result = JsonNodeFactory.instance.arrayNode();
            for (JsonNode sourceNode : sourceArray) {
                if (!sourceNode.isObject() || sourceNode.isEmpty()) {
                    continue;
                }
                ObjectNode node = convertNode((ObjectNode) sourceNode);
                if (!node.isNull()) {
                    result.add(node);
                }
            }
            exchange.getIn().setBody(body);
        } else {
            log.warn(">>> withdrawal list payload is not an array");
        }
    }

    private ObjectNode convertNode(ObjectNode sourceNode) {
        final String personTypeField = "personType";
        JsonNode personTypeNode = sourceNode.get(personTypeField);
        if (Objects.nonNull(personTypeNode)) {
            PersonType personType = PersonType.findNabDetailCode(Integer.parseInt(personTypeNode.asText()));
            sourceNode.put(personTypeField, personType.name());
        }
        return sourceNode;
    }
}
