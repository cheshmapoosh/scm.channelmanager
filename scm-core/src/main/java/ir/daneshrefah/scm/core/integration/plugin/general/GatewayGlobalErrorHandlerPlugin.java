package ir.daneshrefah.scm.core.integration.plugin.general;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

import static ir.daneshrefah.scm.common.model.plugin.PluginType.TRANSFORMER;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayGlobalErrorHandlerPlugin implements PluginHandler {

    @Override
    public PluginType getType() {
        return TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        routeDefinition
                .process(exchange -> {
                    log.info("Processing Exchange Body");
                });
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {

    }

}
