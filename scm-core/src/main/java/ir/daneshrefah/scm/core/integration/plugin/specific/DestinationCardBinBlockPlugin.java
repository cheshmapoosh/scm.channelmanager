package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.config.DestinationCardBinBlockConfig;
import ir.daneshrefah.scm.core.config.DestinationCardBinBlockProperties;
import ir.daneshrefah.scm.core.integration.plugin.support.DestinationCardNumberResolver;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DestinationCardBinBlockPlugin implements PluginHandler {

    private static final int MIN_CARD_LENGTH = 16;
    private static final int MAX_CARD_LENGTH = 19;
    private static final int BIN_LENGTH = 6;

    private final DestinationCardBinBlockProperties properties;
    private final DestinationCardNumberResolver cardNumberResolver;
    private final ObjectMapper objectMapper;

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    public PluginScope getScope() {
        return PluginScope.SERVICE;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> routeProperties) {
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) {
        DestinationCardBinBlockConfig config = resolveConfig(pluginDetail);

        handleWithConfig(exchange, pluginDetail, config);
    }

    private String normalize(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        return cardNumber.replaceAll("[\\s-]", "");
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null
                && cardNumber.length() >= MIN_CARD_LENGTH
                && cardNumber.length() <= MAX_CARD_LENGTH
                && cardNumber.matches("[0-9]+");
    }

    private DestinationCardBinBlockConfig resolveConfig(PluginDetail pluginDetail) {
        if (pluginDetail == null || pluginDetail.getConfig() == null || pluginDetail.getConfig().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.convertValue(pluginDetail.getConfig(), DestinationCardBinBlockConfig.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Could not convert "
                            + "destinationCardBinBlockPlugin "
                            + "database config. plugin="
                            + pluginDetail.getName(),
                    exception);
        }
    }

    private void handleWithConfig(Exchange exchange, PluginDetail pluginDetail, DestinationCardBinBlockConfig config) {
        if (!properties.isEnabled()
                || pluginDetail == null
                || Boolean.FALSE.equals(pluginDetail.getActive())
                || properties.getBlockedBankBins().isEmpty()) {
            return;
        }

        if (config == null || config.getCardNumberSources() == null || config.getCardNumberSources().isEmpty()) {
            return;
        }

        String normalizedCardNumber = normalize(cardNumberResolver.resolve(exchange.getMessage(), config.getCardNumberSources()));
        if (!isValidCardNumber(normalizedCardNumber)) {
            // Request validation remains the responsibility of the operation's existing validators.
            return;
        }

        String bin = normalizedCardNumber.substring(0, BIN_LENGTH);
        if (properties.getBlockedBankBins().contains(bin)) {
            DestinationCardBinBlockProperties.Error error = properties.getError();
            throw new DestinationCardBinNotAllowedException(error.getCode());
        }
    }

}