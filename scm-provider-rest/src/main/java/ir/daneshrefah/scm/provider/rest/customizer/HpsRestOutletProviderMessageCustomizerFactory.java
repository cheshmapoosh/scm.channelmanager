package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class HpsRestOutletProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<HpsRestOutletProviderMessageCustomizerFactory.Config> {
    public static final String TYPE = "hps-rest-outlet";

    @Override
    public String type() {
        return TYPE;
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
        if (context == null || !"rest".equalsIgnoreCase(context.transportType())) {
            throw new IllegalArgumentException("hps-rest-outlet customizer can only be configured for REST providers");
        }
        Config safeConfig = config == null ? new Config() : config;
        safeConfig.validate(context.providerCode());
        return new OutletCustomizer(safeConfig.location, safeConfig.name, safeConfig.value);
    }

    @Getter
    @Setter
    public static class Config {
        private String location = "body";
        private String name = "outlet";
        private String value;

        void validate(String providerCode) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("hps-rest-outlet.name is required for provider " + providerCode);
            }
            if (StringUtils.isBlank(value)) {
                throw new IllegalArgumentException("hps-rest-outlet.value is required for provider " + providerCode);
            }
        }
    }

    private record OutletCustomizer(String location, String name, String value) implements ProviderMessageCustomizer {
        @Override
        public int order() {
            return 100;
        }

        @Override
        public void beforeSend(ProviderExchange exchange) {
            String resolvedLocation = StringUtils.defaultIfBlank(location, "body").trim().toLowerCase();
            switch (resolvedLocation) {
                case "header" -> exchange.request().putHeader(name, value);
                case "query" -> exchange.request().putQueryParameter(name, value);
                case "body" -> exchange.request().putField(name, value);
                default -> throw new IllegalArgumentException("Unsupported hps-rest-outlet.location: " + location);
            }
        }
    }
}
