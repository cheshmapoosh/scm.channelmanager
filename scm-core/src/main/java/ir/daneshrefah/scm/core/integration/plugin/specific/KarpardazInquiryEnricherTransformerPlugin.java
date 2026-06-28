package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.mfathi91.time.PersianDate;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component("karpardazInquiryEnricherTransformerPlugin")
@AllArgsConstructor
@Slf4j
public class KarpardazInquiryEnricherTransformerPlugin implements PluginHandler {

    private final ObjectMapper objectMapper;

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        PluginPhase phase = pluginDetail.getPhase();
        if (!PluginPhase.AFTER.equals(phase)) {
            throw new IllegalArgumentException("Plugin phase " + phase + " is not supported on 'authenticatedPersonEnricherTransformerPlugin'");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        if(true){
            return;
        }
        Object inBody = exchange.getIn().getBody();
        JsonNode jsonBody;
        if (inBody instanceof String) {
            jsonBody = objectMapper.readTree(inBody.toString());
        } else {
            jsonBody = objectMapper.valueToTree(inBody);
        }

        if (jsonBody.isNull() || !jsonBody.isArray()) {
            log.warn("{} response is not an array", exchange.getProperty(Message.OPERATION, String.class));
            return;
        }

        String channelCode = exchange.getProperty(Message.CHANNEL_CODE, String.class);

        ArrayNode result = JsonNodeFactory.instance.arrayNode();

        for (JsonNode node : jsonBody) {
            JsonNode expireDateRaw = node.get("expireDate");
            int year = Integer.parseInt(expireDateRaw.asText().trim().substring(0, 4));
            int month = Integer.parseInt(expireDateRaw.asText().trim().substring(4, 6));
            int day = Integer.parseInt(expireDateRaw.asText().trim().substring(6, 8));

            PersianDate persianDate = PersianDate.of(year, month, day);
            LocalDate expireDate = persianDate.toGregorian();
            LocalDate currentDate = LocalDate.now();

            if (expireDate.isBefore(currentDate)) {
                continue;
            }

            JsonNode permitService = node.get("permitServiceId");
            for (JsonNode permitServiceNode : permitService) {
                if (permitServiceNode.asText().toLowerCase().trim().equals(channelCode.toLowerCase().trim())) {
                    result.add(node);
                }
            }
        }

        exchange.getIn().setBody(result);
    }
}
