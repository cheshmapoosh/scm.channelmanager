package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class DefaultRestUrlBuilder implements RestUrlBuilder {

    @Override
    public RestUrl build(TerminalServiceChannelAccess service) {
        String httpMethod = findHttpMethodByServiceType(service.getTerminalServiceAccess().getService().getType());
        String url = generateServiceUrl(service);
        return new RestUrl(httpMethod, url);
    }

    private String generateServiceUrl(TerminalServiceChannelAccess channelAccess) {
        String terminalCode = channelAccess.getTerminalServiceAccess().getTerminal().getCode();
        String serviceUrl = extractServiceUrl(channelAccess.getTerminalServiceAccess().getService());
        StringBuilder urlBuilder = new StringBuilder("api/");
        urlBuilder
                .append(terminalCode)
                .append(serviceUrl);
        return urlBuilder.toString();
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

    private String findHttpMethodByServiceType(ServiceType type) {
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

}
