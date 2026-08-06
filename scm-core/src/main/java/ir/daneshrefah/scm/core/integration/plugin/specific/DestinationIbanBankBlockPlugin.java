package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.config.DestinationIbanBankBlockConfig;
import ir.daneshrefah.scm.core.config.DestinationIbanBankBlockProperties;
import ir.daneshrefah.scm.core.integration.plugin.support.DestinationIbanResolver;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DestinationIbanBankBlockPlugin implements PluginHandler {

    private static final int IRAN_IBAN_LENGTH = 26;
    private static final int BANK_CODE_START_WITH_IR = 4;
    private static final int BANK_CODE_START_WITHOUT_IR = 2;
    private static final int BANK_CODE_END_WITH_IR = 7;
    private static final int BANK_CODE_END_WITHOUT_IR = 5;

    private final DestinationIbanBankBlockProperties properties;
    private final DestinationIbanResolver ibanResolver;
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
        DestinationIbanBankBlockConfig config = this.resolveConfig(pluginDetail);

        handleWithConfig(exchange, pluginDetail, config);
    }

    private String normalize(String iban) {
        if (iban == null) {
            return null;
        }
        return iban.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private boolean isValidIranianIban(String iban) {
        if (iban == null || iban.length() != IRAN_IBAN_LENGTH || !iban.matches("IR[0-9]{24}")) {
            return false;
        }
        int checkDigits = Integer.parseInt(iban.substring(2, 4));
        if (checkDigits < 2) {
            return false;
        }
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;
        for (int index = 0; index < rearranged.length(); index++) {
            char character = rearranged.charAt(index);
            if (Character.isLetter(character)) {
                int value = character - 'A' + 10;
                remainder = (remainder * 10 + value / 10) % 97;
                remainder = (remainder * 10 + value % 10) % 97;
            } else {
                remainder = (remainder * 10 + character - '0') % 97;
            }
        }
        return remainder == 1;
    }

    private DestinationIbanBankBlockConfig resolveConfig(PluginDetail pluginDetail) {
        if (pluginDetail == null || pluginDetail.getConfig() == null || pluginDetail.getConfig().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.convertValue(pluginDetail.getConfig(), DestinationIbanBankBlockConfig.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Could not convert destinationIbanBankBlockPlugin database config. plugin="
                            + pluginDetail.getName(),
                    exception);
        }
    }

    private void handleWithConfig(Exchange exchange, PluginDetail pluginDetail, DestinationIbanBankBlockConfig config) {
        if (!properties.isEnabled()
                || pluginDetail == null
                || Boolean.FALSE.equals(pluginDetail.getActive())
                || properties.getBlockedBankCodes().isEmpty()) {
            return;
        }

        if (config == null || config.getIbanSources() == null || config.getIbanSources().isEmpty()) {
            return;
        }

        String normalizedIban = normalize(ibanResolver.resolve(exchange.getMessage(), config.getIbanSources()));
        if (!isValidIranianIban(normalizedIban)) {
            // Request validation remains the responsibility of the operation's existing validators.
            return;
        }

        String bankCode;
        if (normalizedIban.startsWith("IR")) {
            bankCode = normalizedIban.substring(BANK_CODE_START_WITH_IR, BANK_CODE_END_WITH_IR);
        } else {
            bankCode = normalizedIban.substring(BANK_CODE_START_WITHOUT_IR, BANK_CODE_END_WITHOUT_IR);
        }
        this.validateBankCodeAllowed(bankCode);
    }

    private void validateBankCodeAllowed(String bankCode) {
        if (properties.getBlockedBankCodes().contains(bankCode)) {
            DestinationIbanBankBlockProperties.Error error = properties.getError();
            throw new DestinationIbanBankNotAllowedException(error.getCode());
        }
    }

}