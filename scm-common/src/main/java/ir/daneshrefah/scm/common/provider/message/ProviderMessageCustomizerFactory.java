package ir.daneshrefah.scm.common.provider.message;

public interface ProviderMessageCustomizerFactory<C> {

    String type();

    Class<C> configType();

    int defaultOrder();

    ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, C config);
}
