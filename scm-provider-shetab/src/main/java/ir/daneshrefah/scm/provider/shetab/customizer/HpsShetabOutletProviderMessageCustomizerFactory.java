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
public class HpsShetabOutletProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<HpsShetabOutletProviderMessageCustomizerFactory.Config> {
    @Override
    public String type() {
        return "hps-shetab-outlet";
    }

    @Override
    public Class<Config> configType() {
        return Config.class;
    }

    @Override
    public int defaultOrder() {
        return 100;
    }

    @Override
    public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
        validateContext(context, type());
        Config safe = config == null ? new Config() : config;
        safe.validate(context.providerCode(), type());
        return new FieldCustomizer(safe.field, safe.value, defaultOrder());
    }

    static void validateContext(ProviderMessageCustomizerFactoryContext context, String type) {
        if (context == null || !"shetab".equalsIgnoreCase(context.providerType()) || "rest".equalsIgnoreCase(context.transportType())) {
            throw new IllegalArgumentException(type + " customizer can only be configured for Shetab providers");
        }
    }

    @Getter
    @Setter
    public static class Config {
        private Integer field;
        private String value;

        void validate(String providerCode, String type) {
            if (field == null || field < 1) {
                throw new IllegalArgumentException(type + ".field is required for provider " + providerCode);
            }
            if (StringUtils.isBlank(value)) {
                throw new IllegalArgumentException(type + ".value is required for provider " + providerCode);
            }
        }
    }

    record FieldCustomizer(int field, String value, int order) implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            ISOMsg msg = ShetabCustomizerSupport.isoMessage(exchange);
            ShetabCustomizerSupport.setIsoField(exchange, msg, field, value);
        }
    }
}
