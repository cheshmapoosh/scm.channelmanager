package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public class RestInboundChannelGenerator extends AbstractInboundChannelGenerator {

    private CamelContext camelContext;
    private CamelRouteBuilder routeBuilder;

    public RestInboundChannelGenerator(ApplicationContext applicationContext, ServiceProducerTemplate producerTemplate, Channel channel) {
        super(applicationContext, producerTemplate, channel);
    }

    @Override
    protected void initConfig() {
        Map<String, CamelContext> beanMap  = applicationContext.getBeansOfType(CamelContext.class);
        if (!beanMap.isEmpty()) {
            camelContext = beanMap.values().iterator().next();
        }
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

    public static String getProtocolKey() {
        return "REST";
    }

}
