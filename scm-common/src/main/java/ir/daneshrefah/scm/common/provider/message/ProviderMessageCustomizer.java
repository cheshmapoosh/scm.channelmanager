package ir.daneshrefah.scm.common.provider.message;

public interface ProviderMessageCustomizer {

    int order();

    default void beforeSend(ProviderExchange exchange) {
    }

    default void afterReceive(ProviderExchange exchange) {
    }
}
