package ir.daneshrefah.scm.provider.shetab.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.security.ShetabSecurityCrypto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ShetabMacProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<ShetabMacProviderMessageCustomizerFactory.Config> {
    private final ShetabPackagerFactory packagerFactory;

    @Override
    public String type() {
        return "shetab-mac";
    }

    @Override
    public Class<Config> configType() {
        return Config.class;
    }

    @Override
    public int defaultOrder() {
        return 10000;
    }

    @Override
    public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
        HpsShetabOutletProviderMessageCustomizerFactory.validateContext(context, type());
        Config safe = config == null ? new Config() : config;
        safe.validate(context.providerCode());
        return new MacCustomizer(safe.key, safe.field, safe.verifyResponse, safe.placeholder,
                safe.packedLengthBytes, defaultOrder(), packagerFactory, new ShetabSecurityCrypto());
    }

    @Getter
    @Setter
    public static class Config {
        private String key;
        private Integer field = 128;
        private Boolean verifyResponse = false;
        private String placeholder = "AAAAAAAAAAAAAAAA";
        private Integer packedLengthBytes = 16;

        void validate(String providerCode) {
            if (StringUtils.isBlank(key)) {
                throw new IllegalArgumentException("shetab-mac.key is required for provider " + providerCode);
            }
        }
    }

    record MacCustomizer(
            String key,
            int field,
            Boolean verifyResponse,
            String placeholder,
            int packedLengthBytes,
            int order,
            ShetabPackagerFactory packagerFactory,
            ShetabSecurityCrypto crypto
    ) implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            ISOMsg msg = ShetabCustomizerSupport.isoMessage(exchange);
            ShetabResolvedConfig config = ShetabCustomizerSupport.resolvedConfig(exchange);
            msg.setPackager(packagerFactory.create(config));
            msg.unset(field);
            try {
                ShetabCustomizerSupport.setIsoField(exchange, msg, field, calculateMac(msg));
            } catch (ISOException e) {
                throw new IllegalStateException("Could not generate Shetab request MAC for provider=" + exchange.context().providerCode(), e);
            }
        }

        @Override
        public void afterReceive(ProviderExchange exchange) {
            if (!Boolean.TRUE.equals(verifyResponse)) {
                return;
            }
            ISOMsg response = ShetabCustomizerSupport.isoResponse(exchange);
            ShetabResolvedConfig config = ShetabCustomizerSupport.resolvedConfig(exchange);
            String receivedMac = ShetabCustomizerSupport.safeField(response, field);
            if (StringUtils.isBlank(receivedMac)) {
                throw new IllegalStateException("Shetab response MAC field is empty for provider=" + exchange.context().providerCode());
            }
            try {
                response.setPackager(packagerFactory.create(config));
                String calculatedMac = calculateMac(response);
                response.set(field, receivedMac);
                if (!receivedMac.equalsIgnoreCase(calculatedMac)) {
                    throw new IllegalStateException("Shetab response MAC verification failed for provider=" + exchange.context().providerCode());
                }
            } catch (ISOException e) {
                throw new IllegalStateException("Could not verify Shetab response MAC for provider=" + exchange.context().providerCode(), e);
            }
        }

        private String calculateMac(ISOMsg message) throws ISOException {
            String resolvedPlaceholder = StringUtils.defaultIfBlank(placeholder, "AAAAAAAAAAAAAAAA");
            message.set(field, resolvedPlaceholder);
            byte[] packed = message.pack();
            int macLength = packedLengthBytes > 0 ? packedLengthBytes : resolvedPlaceholder.length();
            if (packed.length <= macLength) {
                throw new IllegalStateException("Packed Shetab message is shorter than configured MAC length");
            }
            byte[] macInput = Arrays.copyOf(packed, packed.length - macLength);
            return crypto.generateIso9797Mac(macInput, key);
        }
    }
}
