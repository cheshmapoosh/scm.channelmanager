package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationDefinitionType;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.model.operation.RestConfigOperationDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RestOperationTypeHandler implements OperationTypeHandler {
    @Override
    public OperationType getOperationType() {
        return OperationType.REST;
    }

    @Override
    public void config(RouteDefinition route, Operation operation) {
        RestConfigOperationDefinition restConfigOperationDefinition = operation.getDefinitions().stream()
                .filter(operationDefinition -> Objects.equals(operationDefinition.getType(), OperationDefinitionType.REST_CONFIG))
                .findFirst()
                .map(RestConfigOperationDefinition.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Any definition for rest config not found"));

        String url = operation.getPath();
        if (StringUtils.isEmpty(url)) {
            url = restConfigOperationDefinition.getUrl();
        }
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("An empty url for rest config. both operation.path and operationDefinition.url is empty");
        }
        HttpMethod httpMethod = restConfigOperationDefinition.getHttpMethod();
        if (httpMethod == null) {
            throw new IllegalArgumentException("An empty http method for rest config");
        }
        StringBuilder targetUrl = new StringBuilder("webclient:" + url +
                "?method=" + httpMethod.getValue());
        Integer responseTimeout = restConfigOperationDefinition.getResponseTimeout();
        if (responseTimeout != null) {
            targetUrl.append("&responseTimeout=").append(responseTimeout);
        }
        Integer connectTimeout = restConfigOperationDefinition.getConnectTimeout();
        if (connectTimeout != null) {
            targetUrl.append("&connectTimeout=").append(connectTimeout);
        }
        Integer writeTimeout = restConfigOperationDefinition.getWriteTimeout();
        if (writeTimeout != null) {
            targetUrl.append("&writeTimeout=").append(writeTimeout);
        }
        Boolean retryEnabled = restConfigOperationDefinition.getRetryEnabled();
        if (retryEnabled != null) {
            targetUrl.append("&retryEnabled=").append(retryEnabled);
        }
        Integer maxAttempts = restConfigOperationDefinition.getMaxAttempts();
        if (maxAttempts != null) {
            targetUrl.append("&maxAttempts=").append(maxAttempts);
        }
        Integer minBackoff = restConfigOperationDefinition.getMinBackoff();
        if (minBackoff != null) {
            targetUrl.append("&minBackoff=").append(minBackoff);
        }
        Boolean wiretap = restConfigOperationDefinition.getWiretap();
        if (wiretap != null) {
            targetUrl.append("&wiretap=").append(wiretap);
        }
        
        route.to(targetUrl.toString());
    }
}
