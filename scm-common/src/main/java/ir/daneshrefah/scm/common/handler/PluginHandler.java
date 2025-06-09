package ir.daneshrefah.scm.common.handler;

import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;

import java.util.Map;

public interface PluginHandler {
    PluginType getType();

    void init (RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties);

    void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception;
}