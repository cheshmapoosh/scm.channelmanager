package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.*;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class AtpsOperationTypeHandler implements OperationTypeHandler {
    @Override
    public OperationType getOperationType() {
        return OperationType.ATPS;
    }

    @Override
    public void config(RouteDefinition route, Operation operation) {
        TcpConfigOperationDefinition tcpConfigOperationDefinition = operation.getDefinitions().stream()
                .filter(operationDefinition -> Objects.equals(operationDefinition.getType(), OperationDefinitionType.TCP_CONFIG))
                .findFirst()
                .map(TcpConfigOperationDefinition.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Any definition for tcp config not found"));
        route.transform().language("groovy", """
                def body = request.getBody(String.class);
                    if(body == null){
                        body = "";
                    }
                    return body;
        """);

        String url = operation.getPath();
        if (StringUtils.isEmpty(url)) {
            url = tcpConfigOperationDefinition.getUrl();
        }
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("An empty url for tcp config. both operation.path and operationDefinition.url is empty");
        }
        StringBuilder urlAttachment = new StringBuilder();
        String command = tcpConfigOperationDefinition.getCommand();
        if (StringUtils.isEmpty(command)) {
            log.error("An empty command for atps tcp config. operation id: {}", operation.getId());
        } else {
            urlAttachment.append("&command=").append(command);
        }


        Integer connectTimeout = tcpConfigOperationDefinition.getConnectTimeout();
        if (connectTimeout != null) {
            urlAttachment.append("&connectTimeout=").append(connectTimeout);
        }

        Boolean tcpNoDelay = tcpConfigOperationDefinition.isTcpNoDelay();
        if (tcpNoDelay != null) {
            urlAttachment.append("&tcpNoDelay=").append(tcpNoDelay);
        }

        String ackEquals = tcpConfigOperationDefinition.getAckEquals();
        if (ackEquals != null && ackEquals.length() > 0) {
            urlAttachment.append("&ackEquals=").append(ackEquals);
        }

        Integer requestTimeout = tcpConfigOperationDefinition.getRequestTimeout();
        if (requestTimeout != null) {
            urlAttachment.append("&requestTimeout=").append(requestTimeout);
        }

        Boolean validateAck = tcpConfigOperationDefinition.getValidateAck();
        if (validateAck != null) {
            urlAttachment.append("&validateAck=").append(validateAck);
        }

        Boolean keepAlive = tcpConfigOperationDefinition.isKeepAlive();
        if (keepAlive != null) {
            urlAttachment.append("&keepAlive=").append(keepAlive);
        }

        String targetUrl = "atps:" + url;
        if (!urlAttachment.isEmpty()) {
            targetUrl += "?" + urlAttachment.toString().substring(1);
        }

        route.to(targetUrl);
    }
}
