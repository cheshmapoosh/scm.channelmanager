package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
@Deprecated
public class DefaultRestUrlBuilder implements RestUrlBuilder {

    @Override
    public RestUrl build(TerminalServiceAccess serviceAccess) {
        String httpMethod = findHttpMethodByServiceType(serviceAccess.getService().getType());
        String url = generateServiceUrl(serviceAccess);
        return new RestUrl(httpMethod, url);
    }

    private String generateServiceUrl(TerminalServiceAccess serviceAccess) {
        String terminalCode = serviceAccess.getTerminal().getCode();
        String parentServiceUrl = extractServiceUrl(serviceAccess.getService().getParent());
        String serviceUrl = extractServiceUrl(serviceAccess.getService());
        String version = extractServiceVersion(serviceAccess.getService());
        String urlBuilder = "api" +
                            version +
                            (StringUtils.isEmpty(parentServiceUrl) ? "" : fixUrlPattern(parentServiceUrl)) +
                            fixUrlPattern(serviceUrl);
        return urlBuilder;
    }

    private String extractServiceVersion(ScmService service) {
        if (null == service) {
            return null;
        }
        String result = null != service.getVersion() ? String.valueOf(service.getVersion()) : "1";
        return "/v" + result;
    }

    private String extractServiceUrl(ScmService service) {
        if (null == service) {
            return null;
        }
        String serviceUrl = null != service.getAlias() ? service.getAlias() : service.getCode().toLowerCase();
        if (!StringUtils.startsWith(serviceUrl, "/", true)) {
            serviceUrl = "/" + serviceUrl;
        }
        return serviceUrl.replace("_", "-");
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
                return "post";
            case FINANCE:
                return "post";
            case ENTITY_CREATE:
                return "post";
            case ENTITY_UPDATE:
                return "put";
            case ENTITY_DELETE:
                return "delete";
            default:
                return defaultMethod;
        }
    }

    private String fixUrlPattern(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        if (!StringUtils.startsWith(inputString, "/", false)) {
            inputString = "/" + inputString;
        }
        if (inputString.endsWith("/")) {
            return inputString.substring(0, inputString.length() - 1);
        } else {
            return inputString; // Already in the desired format
        }
    }
}
