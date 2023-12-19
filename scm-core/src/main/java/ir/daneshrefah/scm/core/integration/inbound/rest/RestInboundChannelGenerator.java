package ir.daneshrefah.scm.core.integration.inbound.rest;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private AuthenticationClientTemplate authenticationClientTemplate;
    private CamelRouteBuilder routeBuilder;

    @Override
    protected void initConfig() {
//        Map<String, CamelContext> beanMap  = applicationContext.getBeansOfType(CamelContext.class);
//        if (!beanMap.isEmpty()) {
//            camelContext = beanMap.values().iterator().next();
//        }
        routeBuilder = new CamelRouteBuilder(channel, this::invokeService, this::prepareServiceUrl,
                this::createHttpMethodBasedOnServiceType, authenticationClientTemplate);
        /*try {
            camelContext.addRoutes(new CamelRouteBuilder(channel, channelAccesses));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    }

    private String prepareServiceUrl(TerminalServiceChannelAccess channelAccess) {
        String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
//        String httpMethod = createHttpMethodBasedOnServiceType(channelAccess);
        String serviceUrl = extractServiceUrl(channelAccess.getTerminalServiceAccess().getService());
        String parentServiceUrl = extractServiceUrl(channelAccess.getTerminalServiceAccess().getService().getParent());
        StringBuilder urlBuilder = new StringBuilder("api/");
        urlBuilder
//                .append(httpMethod)
//                .append(":api/")
                .append(terminalCode)
                .append(null != parentServiceUrl ? parentServiceUrl : "")
                .append(serviceUrl);
        return urlBuilder.toString();
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

    private String createHttpMethodBasedOnServiceType(TerminalServiceChannelAccess channelAccess) {
        ServiceType type = channelAccess.getTerminalServiceAccess().getService().getType();
        String defaultMethod = "post";
        if (null == type) {
            return defaultMethod;
        }
        switch (type) {
            case INQUIRY:
                return "get";
            case REPORT:
                return "get";
            case FINANCE:
                return "post";
            default:
                return defaultMethod;
        }
    }

    private String extractServiceUrl(Service service) {
        if (null == service) {
            return null;
        }
        String serviceUrl = StringUtils.isNotEmpty(service.getAlias()) ? service.getAlias() : service.getCode();
        if (!StringUtils.startsWith(serviceUrl, "/", true)) {
            serviceUrl = "/" + serviceUrl;
        }
        return serviceUrl.toLowerCase().replace("_", "-");
    }

}
