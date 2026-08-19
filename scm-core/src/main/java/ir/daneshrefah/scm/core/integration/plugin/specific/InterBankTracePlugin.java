package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.config.InterBankTraceConfig;
import ir.daneshrefah.scm.core.integration.plugin.support.PluginMessageValueReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component("interBankTracePlugin")
public class InterBankTracePlugin implements PluginHandler {

    private static final int IRAN_CARD_PREFIX_LENGTH = 6;

    private final ObjectMapper objectMapper;
    private final PluginMessageValueReader valueReader;

    @Override
    public PluginType getType() {
        return PluginType.LOGGER;
    }

    public PluginScope getScope() {
        return PluginScope.SERVICE;
    }

    @Override
    public void init(RouteDefinition route, PluginDetail detail, Map<String, ?> props) {
        if (detail.getPhase() != PluginPhase.BEFORE) {
            throw new IllegalArgumentException("InterBankTracePlugin only supports BEFORE phase");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail detail) {
        InterBankTraceConfig config = resolveConfig(detail);
        Map<String, String> requestFields = resolveRequestFields(exchange, config);

        if (requestFields.isEmpty()) {
            log.warn("InterBankTracePlugin: no request fields extracted for plugin = {}", detail.getName());
            return;
        }

        Collection<String> requestValues = requestFields.values();

        String sourceCardNumber = requestValues.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No value for index 0"));

        String destinationCardNumber = requestValues.stream()
                .skip(1)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No value for index 1"));

        boolean isInterBank = !sourceCardNumber.substring(0, IRAN_CARD_PREFIX_LENGTH).equals(destinationCardNumber.substring(0, IRAN_CARD_PREFIX_LENGTH));

        exchange.setProperty("scm.service.inter.bank", isInterBank);
    }

    private Map<String, String> resolveRequestFields(Exchange exchange, InterBankTraceConfig config) {
        Map<String, String> resolvedFields = new LinkedHashMap<>();

        Message message = exchange.getMessage();

        String body = message.getBody(String.class);
        message.setBody(body);

        config.getRequestFields().forEach((fieldName, sources) -> {
            String value = valueReader.findFirstValue(message, sources);
            if (value != null) {
                resolvedFields.put(fieldName, value);
            }
        });
        return resolvedFields;
    }

    private InterBankTraceConfig resolveConfig(PluginDetail detail) {
        if (detail == null || detail.getConfig() == null || detail.getConfig().isEmpty()) {
            log.error("InterBankTracePlugin config is null or empty");
            throw new IllegalArgumentException("InterBankTracePlugin requires database configuration");
        }
        try {
            InterBankTraceConfig config = objectMapper.convertValue(detail.getConfig(), InterBankTraceConfig.class);
            if (config.getRequestFields() == null || config.getRequestFields().isEmpty()) {
                log.error("InterBankTracePlugin requires a non-empty request-fields configuration");
                throw new IllegalArgumentException("InterBankTracePlugin requires a non-empty request-fields configuration");
            }
            return config;
        } catch (IllegalArgumentException exception) {
            log.error("Could not read InterBankTracePlugin database config. plugin={}, {}", detail.getName(), exception);
            throw new IllegalArgumentException("Could not read InterBankTracePlugin database config. plugin=" + detail.getName(), exception);
        }
    }

}