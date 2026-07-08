package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContract;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractVersionResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.RequestContractDecoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetProperties;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowRole;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowServiceEntrypointRouteBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskWorkflowGatewayInboundRouteBuilder extends RouteBuilder {
    private static final String DEFAULT_GATEWAY_PATH = "/gateway";
    private static final String SERVICE_CODE_PATH_VARIABLE = "serviceCode";

    private final RuntimeRouteActivation runtimeRouteActivation;
    private final GatewayService gatewayService;
    private final ClientContractResolver clientContractResolver;
    private final Map<String, RequestContractDecoder> requestContractDecoders;
    private final IncomingChannelCodeResolver incomingChannelCodeResolver;
    private final ScmExchangeMdc scmExchangeMdc;
    private final GlobalErrorHandler globalErrorHandler;

    @Override
    public void configure() {
        List<RuntimeTargetProperties> runtimeTargets = runtimeRouteActivation.runtimeTargets();
        runtimeTargets.stream()
                .filter(RuntimeTargetProperties::enabled)
                .forEach(runtimeTarget -> runtimeTarget.gatewayNames()
                        .forEach(gatewayName -> configureGateway(runtimeTarget, gatewayName)));
    }

    private void configureGateway(RuntimeTargetProperties runtimeTarget, String gatewayName) {
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(gatewayName);
        if (gatewayChannel == null || !Boolean.TRUE.equals(gatewayChannel.getActive())) {
            return;
        }
        RuntimeTargetKind targetKind = runtimeRouteActivation.resolveTargetKind(gatewayChannel);
        if (runtimeTarget.targetKind() != targetKind) {
            return;
        }
        if (gatewayChannel.getProtocolType() != ProtocolType.REST) {
            return;
        }
        for (FixedTaskWorkflowInbound inbound : FixedTaskWorkflowInbound.values()) {
            configureInbound(gatewayChannel, targetKind, inbound);
        }
    }

    private void configureInbound(
            GatewayChannel gatewayChannel,
            RuntimeTargetKind targetKind,
            FixedTaskWorkflowInbound inbound
    ) {
        String serviceVersion = ClientContractVersionResolver.DEFAULT_VERSION;
        String routeUri = routeUri(gatewayChannel, inbound.pathSegment());
        String routeId = "gw."
                + RouteIdSupport.targetKindShort(targetKind)
                + "."
                + RouteIdSupport.normalizeGatewayScopeName(gatewayChannel.getName())
                + ".task-workflow."
                + inbound.pathSegment();

        RouteDefinition route = from(routeUri)
                .routeId(routeId)
                .setProperty(Message.GATEWAY_CHANNEL, constant(gatewayChannel))
                .setProperty(Message.GATEWAY_NAME, constant(gatewayChannel.getName()))
                .setProperty(Message.GATEWAY_CHANNEL_PROTOCOL, constant(gatewayChannel.getProtocolType()))
                .setProperty(Message.SERVICE_VERSION, constant(serviceVersion))
                .setProperty(Message.TASK_WORKFLOW_ROLE, constant(inbound.role().name()));

        defineExceptionHandler(route);
        route.onCompletion()
                .process(exchange -> scmExchangeMdc.clear())
                .end();
        route.process(exchange -> {
            applyIncomingContext(exchange);
            ClientContract contract = clientContractResolver.resolve(
                    gatewayChannel,
                    null,
                    serviceVersion
            );
            exchange.setProperty(Message.CLIENT_CONTRACT, contract);
            resolveRequestDecoder(contract).decode(exchange, contract);
        });
        route.to(TaskWorkflowServiceEntrypointRouteBuilder.ROUTE_URI);
        route.to(Routes.GLOBAL_RESPONSE_HANDLER);

        log.info("Registered fixed task workflow gateway route routeId={} uri={} role={}",
                routeId, routeUri, inbound.role());
    }

    private void defineExceptionHandler(RouteDefinition route) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    scmExchangeMdc.put(exchange);
                    log.warn("Task workflow gateway route failed routeId={} exchangeId={} failureType={} failureMessage={}",
                            exchange.getFromRouteId(),
                            exchange.getExchangeId(),
                            exception == null ? null : exception.getClass().getSimpleName(),
                            exception == null ? null : exception.getMessage(),
                            exception);
                    globalErrorHandler.handle(exchange);
                })
                .to(Routes.GLOBAL_RESPONSE_HANDLER);
    }

    private void applyIncomingContext(Exchange exchange) {
        incomingChannelCodeResolver.resolve(exchange)
                .ifPresent(channelCode -> exchange.setProperty(Message.CHANNEL_CODE, channelCode));
        Map<String, Object> variables = new LinkedHashMap<>();
        Object serviceCode = exchange.getMessage().getHeader(SERVICE_CODE_PATH_VARIABLE);
        if (serviceCode != null) {
            variables.put(SERVICE_CODE_PATH_VARIABLE, serviceCode);
        }
        exchange.setProperty(Message.INBOUND_PATH_VARIABLES, Map.copyOf(variables));
    }

    private RequestContractDecoder resolveRequestDecoder(ClientContract contract) {
        RequestContractDecoder decoder = requestContractDecoders.get(contract.requestDecoder());
        if (decoder == null) {
            throw new IllegalStateException("Request decoder not found: " + contract.requestDecoder());
        }
        return decoder;
    }

    private String routeUri(GatewayChannel gatewayChannel, String pathSegment) {
        return "rest:post:" + gatewayBasePath(gatewayChannel)
                + "/{" + SERVICE_CODE_PATH_VARIABLE + "}/task-workflow/" + pathSegment;
    }

    private String gatewayBasePath(GatewayChannel gatewayChannel) {
        String path = StringUtils.trimToNull(gatewayChannel.getPath());
        if (path == null) {
            path = DEFAULT_GATEWAY_PATH;
        }
        path = StringUtils.stripEnd(path, "/");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    private enum FixedTaskWorkflowInbound {
        START("start", TaskWorkflowRole.START_PROCESS),
        APPROVE("approve", TaskWorkflowRole.APPROVE_PROCESS),
        COMPLETE("complete", TaskWorkflowRole.COMPLETE_PROCESS),
        CANCEL("cancel", TaskWorkflowRole.CANCEL_PROCESS);

        private final String pathSegment;
        private final TaskWorkflowRole role;

        FixedTaskWorkflowInbound(String pathSegment, TaskWorkflowRole role) {
            this.pathSegment = pathSegment;
            this.role = role;
        }

        String pathSegment() {
            return pathSegment;
        }

        TaskWorkflowRole role() {
            return role;
        }
    }
}
