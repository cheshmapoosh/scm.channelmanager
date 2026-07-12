package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeContributor;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.apache.camel.Exchange;
import org.jpos.iso.ISOMsg;

import java.util.Map;

/**
 * Extension point for safe, explicitly registered Shetab provider event fields.
 */
public interface ShetabProviderTraceAttributeContributor extends ObservationAttributeContributor {
    default void contributeRequestAttributes(
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg request,
            Map<String, Object> attributes
    ) {
    }

    default void contributeResponseAttributes(
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg response,
            Throwable failure,
            Map<String, Object> attributes
    ) {
    }
}
