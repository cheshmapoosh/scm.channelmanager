package ir.daneshrefah.scm.plugin.nab;

import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component()
public class NabResponseValidator implements PluginHandler {
    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {

    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        throw new ScmException();
    }
}
