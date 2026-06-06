package ir.daneshrefah.scm.provider.shetab.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class ShetabExpiryProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<ShetabExpiryProviderMessageCustomizerFactory.Config> {
    @Override
    public String type() {
        return "shetab-expiry";
    }

    @Override
    public Class<Config> configType() {
        return Config.class;
    }

    @Override
    public int defaultOrder() {
        return 200;
    }

    @Override
    public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
        HpsShetabOutletProviderMessageCustomizerFactory.validateContext(context, type());
        Config safe = config == null ? new Config() : config;
        safe.validate(context.providerCode());
        return new ExpiryCustomizer(safe.field, safe.source, defaultOrder());
    }

    @Getter
    @Setter
    public static class Config {
        private Integer field = 14;
        private String source = "security.expiryDate";

        void validate(String providerCode) {
            if (field == null || field < 1) {
                throw new IllegalArgumentException("shetab-expiry.field is required for provider " + providerCode);
            }
        }
    }

    record ExpiryCustomizer(int field, String source, int order) implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            ISOMsg msg = ShetabCustomizerSupport.isoMessage(exchange);
            msg.unset(field);
            String expiry = ShetabCustomizerSupport.sourceValue(exchange, source);
            if (StringUtils.isBlank(expiry)) {
                if (ShetabCustomizerSupport.booleanSource(exchange, "expiryRequired")) {
                    throw new IllegalArgumentException("Shetab expiry value is required for provider=" + exchange.context().providerCode());
                }
                return;
            }
            if (!expiry.matches("\\d{4}")) {
                throw new IllegalArgumentException("Shetab expiry value must be 4 digits (YYMM)");
            }
            ShetabCustomizerSupport.setIsoField(exchange, msg, field, expiry);
        }
    }
}
