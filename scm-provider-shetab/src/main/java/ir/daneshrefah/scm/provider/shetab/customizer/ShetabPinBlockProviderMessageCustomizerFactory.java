package ir.daneshrefah.scm.provider.shetab.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.provider.shetab.security.ShetabSecurityCrypto;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class ShetabPinBlockProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<ShetabPinBlockProviderMessageCustomizerFactory.Config> {
    @Override
    public String type() {
        return "shetab-pin-block";
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
    public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
        HpsShetabOutletProviderMessageCustomizerFactory.validateContext(context, type());
        Config safe = config == null ? new Config() : config;
        safe.validate(context.providerCode());
        return new PinBlockCustomizer(safe.key, safe.field, safe.panField, safe.pinSource, defaultOrder(), new ShetabSecurityCrypto());
    }

    @Getter
    @Setter
    public static class Config {
        private String key;
        private Integer field = 52;
        private Integer panField = 2;
        private String pinSource = "security.pin";

        void validate(String providerCode) {
            if (StringUtils.isBlank(key)) {
                throw new IllegalArgumentException("shetab-pin-block.key is required for provider " + providerCode);
            }
        }
    }

    record PinBlockCustomizer(String key, int field, int panField, String pinSource, int order, ShetabSecurityCrypto crypto)
            implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            ISOMsg msg = ShetabCustomizerSupport.isoMessage(exchange);
            msg.unset(field);
            String pin = ShetabCustomizerSupport.sourceValue(exchange, pinSource);
            if (StringUtils.isBlank(pin)) {
                if (ShetabCustomizerSupport.booleanSource(exchange, "pinRequired")) {
                    throw new IllegalArgumentException("Shetab PIN is required for provider=" + exchange.context().providerCode());
                }
                return;
            }
            String pan = StringUtils.defaultIfBlank(
                    ShetabCustomizerSupport.safeField(msg, panField),
                    ShetabCustomizerSupport.sourceValue(exchange, "security.pan"));
            String pinBlock = crypto.generatePinBlock(pin, pan, key);
            ShetabCustomizerSupport.setIsoField(exchange, msg, field, pinBlock);
        }
    }
}
