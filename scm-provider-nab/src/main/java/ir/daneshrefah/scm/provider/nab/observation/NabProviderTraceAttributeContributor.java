package ir.daneshrefah.scm.provider.nab.observation;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeContributor;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import org.apache.camel.Exchange;

import java.util.Map;

/**
 * Extension point for safe, explicitly registered NAB provider event fields.
 */
public interface NabProviderTraceAttributeContributor extends ObservationAttributeContributor {
    default void contributeRequestAttributes(
            Exchange exchange,
            NabResolvedConfig config,
            JsonNode request,
            Map<String, Object> attributes
    ) {
    }

    default void contributeResponseAttributes(
            Exchange exchange,
            NabResolvedConfig config,
            JsonNode response,
            Throwable failure,
            Map<String, Object> attributes
    ) {
    }
}
