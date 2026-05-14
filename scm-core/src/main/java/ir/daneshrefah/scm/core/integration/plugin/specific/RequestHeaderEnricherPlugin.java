package ir.daneshrefah.scm.core.integration.plugin.specific;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.token.OpenBankingToken;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("RequestHeaderEnricherPlugin")
@RequiredArgsConstructor
public class RequestHeaderEnricherPlugin implements PluginHandler {

    private final CacheManager cacheManager;

    private static final String OPEN_BANKING_TOKEN_CACHE_MAP = "open-banking-token-cache";
    private static final String OPEN_BANKING_TOKEN_KEY = "open-banking-token";

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        PluginPhase phase = pluginDetail.getPhase();
        if (!PluginPhase.BEFORE.equals(phase)) {
            throw new IllegalArgumentException("Plugin phase " + phase + " is not supported on 'requestHeaderEnricherPlugin'");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        OpenBankingToken token = getFromCache(OPEN_BANKING_TOKEN_CACHE_MAP, OPEN_BANKING_TOKEN_KEY, OpenBankingToken.class);
        exchange.getIn().setHeader("Authorization", "Bearer " + token.getAccess_token());
    }

    private <T> T getFromCache(String cacheName, String key, Class<T> valueType) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + cacheName);
        }
        return cache.get(key, valueType);
    }
}
