package ir.daneshrefah.scm.core.integration.inbound.rest;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Component
public class RestInboundChannelGenerator extends AbstractInboundChannelGenerator {

    @Autowired
    private CamelContext camelContext;
    private CamelRouteBuilder routeBuilder;

    @Override
    protected void initConfig() {
//        Map<String, CamelContext> beanMap  = applicationContext.getBeansOfType(CamelContext.class);
//        if (!beanMap.isEmpty()) {
//            camelContext = beanMap.values().iterator().next();
//        }
        routeBuilder = new CamelRouteBuilder(channel, this::invokeService);
        /*try {
            camelContext.addRoutes(new CamelRouteBuilder(channel, channelAccesses));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    }

    @Override
    protected void registerTerminalService(TerminalServiceChannelAccess channelAccess) {
        routeBuilder.addRoute(channelAccess);
    }

    @Override
    protected void finalizeConfig() {
        try {
            camelContext.addRoutes(routeBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*@Override
    public void initInbound() {
        Map<String, CamelContext> beanMap  = applicationContext.getBeansOfType(CamelContext.class);
        if (!beanMap.isEmpty()) {
            camelContext = beanMap.values().iterator().next();
        }
        try {
            camelContext.addRoutes(new CamelRouteBuilder(channel, channelAccesses));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

}
