package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.provider.rest.security.RestSecurityCrypto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class RestShetabPinBlockProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<RestShetabPinBlockProviderMessageCustomizerFactory.Config> {

    @Override
    public String type() {
        return "rest-shetab-pin-block";
    }

    @Override
    public Class<Config> configType() {
        return Config.class;
    }

    @Override
    public int defaultOrder() {
        return 8000;
    }

    @Override
    public ProviderMessageCustomizer create(
            ProviderMessageCustomizerFactoryContext context,
            Config config) {

        final Config safeConfig = config == null ? new Config() : config;

        return new ProviderMessageCustomizer() {

            @Override
            public int order() {
                return 0;
            }

            @Override
            public void beforeSend(ProviderExchange exchange) {

                try {

                    final String pin =
                            RestShetabCustomizerSupport.sourceValue(
                                    exchange,
                                    safeConfig.getPinSource());

                    final String pan =
                            RestShetabCustomizerSupport.sourceValue(
                                    exchange,
                                    safeConfig.getPanSource());

                    if (Objects.isNull(pin) || Objects.isNull(pan)) {
                        return;
                    }
//                    validateInput(pin, pan);

                    final RestSecurityCrypto crypto = new RestSecurityCrypto();

                    final String pinBlock =
                            crypto.generatePinBlock(
                                    pin,
                                    pan,
                                    safeConfig.getKey());

                    updateRequestBody(exchange, pinBlock);

                } catch (Exception ex) {

                    log.error(
                            "Failed to generate PIN block. pinSource={}, panSource={}",
                            safeConfig.getPinSource(),
                            safeConfig.getPanSource(),
                            ex);

                    throw new IllegalStateException(
                            "Failed to generate Shetab PIN block",
                            ex);
                }


            }


        };
    }

    private void validateInput(String pin, String pan) {

        if (StringUtils.isBlank(pin)) {
            throw new IllegalArgumentException("PIN value is missing");
        }

        if (StringUtils.isBlank(pan)) {
            throw new IllegalArgumentException("PAN value is missing");
        }

        if (pan.length() < 16) {
            throw new IllegalArgumentException(
                    "PAN length must be at least 16 digits");
        }
    }

    @SuppressWarnings("unchecked")
    private void updateRequestBody(
            ProviderExchange exchange,
            String pinBlock) {

        Object bodyObject = exchange.request().body();

        if (!(bodyObject instanceof Map)) {
            throw new IllegalStateException(
                    "Request body must be a Map");
        }

        Map<String, Object> body =
                (Map<String, Object>) bodyObject;

        Object dataObject = body.get("data");

        if (!(dataObject instanceof Map)) {
            throw new IllegalStateException(
                    "Request body does not contain a valid 'data' section");
        }

        Map<String, Object> data =
                (Map<String, Object>) dataObject;

        data.put("pin", pinBlock);
    }

    @Getter
    @Setter
    public static class Config {

        private String key;

        private String pinSource = "security.pin";

        private String panSource = "security.pan";

        public void validate(String providerCode) {

            if (StringUtils.isBlank(key)) {
                throw new IllegalArgumentException(
                        "rest-shetab-pin-block.key is required for provider "
                                + providerCode);
            }
        }
    }
}