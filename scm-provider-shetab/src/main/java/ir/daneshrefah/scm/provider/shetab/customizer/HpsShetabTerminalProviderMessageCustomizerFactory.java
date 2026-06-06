package ir.daneshrefah.scm.provider.shetab.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import org.springframework.stereotype.Component;

@Component
public class HpsShetabTerminalProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<HpsShetabOutletProviderMessageCustomizerFactory.Config> {
    @Override
    public String type() {
        return "hps-shetab-terminal";
    }

    @Override
    public Class<HpsShetabOutletProviderMessageCustomizerFactory.Config> configType() {
        return HpsShetabOutletProviderMessageCustomizerFactory.Config.class;
    }

    @Override
    public int defaultOrder() {
        return 101;
    }

    @Override
    public ProviderMessageCustomizer create(
            ProviderMessageCustomizerFactoryContext context,
            HpsShetabOutletProviderMessageCustomizerFactory.Config config
    ) {
        HpsShetabOutletProviderMessageCustomizerFactory.validateContext(context, type());
        HpsShetabOutletProviderMessageCustomizerFactory.Config safe = config == null
                ? new HpsShetabOutletProviderMessageCustomizerFactory.Config()
                : config;
        safe.validate(context.providerCode(), type());
        return new HpsShetabOutletProviderMessageCustomizerFactory.FieldCustomizer(
                safe.getField(), safe.getValue(), defaultOrder());
    }
}
