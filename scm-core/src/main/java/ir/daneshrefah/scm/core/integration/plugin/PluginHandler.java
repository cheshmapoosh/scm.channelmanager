package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.model.plugin.PluginDefinition;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import org.apache.camel.Exchange;

public interface PluginHandler {
    PluginType getType();
    void handle(Exchange exchange, PluginDefinition pluginDefinition) throws Exception;
}