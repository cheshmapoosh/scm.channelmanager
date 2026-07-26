package ir.daneshrefah.scm.core.integration.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.config.RestGatewayIdempotencyProperties;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractVersionResolver;
import ir.daneshrefah.scm.core.integration.gateway.inbound.GatewayInboundPathVariablesBinder;
import ir.daneshrefah.scm.core.integration.gateway.inbound.GatewayInboundRouteActionBinder;
import ir.daneshrefah.scm.core.integration.gateway.inbound.InboundRouteActionConfig;
import ir.daneshrefah.scm.core.integration.gateway.inbound.InboundRouteDefinitionValidator;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.support.ExchangeHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestGatewayInboundRouteFactory implements GatewayInboundRouteFactory {
    private static final Pattern VERSION_PATH_PATTERN = Pattern.compile("^/?v[1-9][0-9]*(?:/.*)?$");
    private static final String CLIENT_CORRELATION_ID_HEADER = "X-SCM-Client-Correlation-ID";//"X-Correlation-Id";

    private final ClientContractVersionResolver clientContractVersionResolver;
    private final InboundRouteDefinitionValidator inboundRouteDefinitionValidator;
    private final GatewayInboundRouteActionBinder inboundRouteActionBinder;
    private final GatewayInboundPathVariablesBinder inboundPathVariablesBinder;
    private final ObjectMapper objectMapper;

    private final IdempotentRepository idempotentRepository;
    private final RestGatewayIdempotencyProperties idempotencyProperties;

    @Override
    public ProtocolType protocol() {
        return ProtocolType.REST;
    }

    @Override
    public void configureGateway(GatewayInboundRouteFactoryContext context) {
        GatewayChannel gatewayChannel = context.gatewayChannel();
        RestConfigurationDefinition restConfigurationDefinition = context.routeBuilder().restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .corsAllowCredentials(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*")
                .corsHeaderProperty("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE")
                .corsHeaderProperty("Access-Control-Allow-Headers", "*")
                .corsHeaderProperty("Access-Control-Allow-Credentials", "true")
                .corsHeaderProperty("Access-Control-Expose-Headers", "*");
        String host = gatewayChannel.getHost();
        if (StringUtils.isNotEmpty(host)) {
            restConfigurationDefinition.host(host);
        }
    }

    @Override
    public List<InboundRouteDefinition> createRoutes(GatewayInboundRouteContext context) {
        return createRestRouteDefinitions(
                context,
                clientContractVersionResolver);
    }

    private List<InboundRouteDefinition> createRestRouteDefinitions(
            GatewayInboundRouteContext context,
            ClientContractVersionResolver clientContractVersionResolver) {
        RuntimeServicePlan servicePlan = context.servicePlan();
        List<InboundRouteDefinition> routeDefinitions = new ArrayList<>();
        Service service = servicePlan.service();
        if (service.getRoutingStrategy() == RoutingStrategy.TASK_WORKFLOW) {
            return createTaskWorkflowCategoryRoutes(
                    context,
                    clientContractVersionResolver
            );
        }
        List<ChannelServiceDefinition> channelServiceDefinitions = servicePlan.routeDefinitions();
        if (channelServiceDefinitions == null || channelServiceDefinitions.isEmpty()) {
            return routeDefinitions;
        }
        List<InboundChannelServiceDefinition> inboundDefinitions = channelServiceDefinitions.stream()
                .filter(definition -> definition.getType() == ChannelServiceDefinitionType.INBOUND)
                .map(definition -> {
                    if (definition instanceof InboundChannelServiceDefinition inboundDefinition) {
                        return inboundDefinition;
                    }
                    throw new IllegalStateException("INBOUND channel service definition "
                            + definition.getId() + " is not an InboundChannelServiceDefinition");
                })
                .toList();
        Map<InboundChannelServiceDefinition, InboundRouteActionConfig> actionConfigs =
                inboundRouteDefinitionValidator.validate(context, inboundDefinitions);
        Set<String> usedRouteIds = new HashSet<>();
        channelServiceDefinitions.forEach(channelServiceDefinition -> {
            if (channelServiceDefinition.getType() == null) {
                return;
            }
            routeDefinitions.addAll(
                    switch (channelServiceDefinition.getType()) {
                        case INBOUND -> createRestRouteDefinition(
                                context,
                                clientContractVersionResolver,
                                service,
                                (InboundChannelServiceDefinition) channelServiceDefinition,
                                actionConfigs.get((InboundChannelServiceDefinition) channelServiceDefinition),
                                usedRouteIds,
                                null);
                        // API_DOC and SVC_DOMAIN_MEMBER are metadata only; they must never create inbound routes.
                        default -> Collections.emptyList();
                    }
            );
        });
        return routeDefinitions;
    }

    private List<InboundRouteDefinition> createRestRouteDefinition(GatewayInboundRouteContext context,
                                                                   ClientContractVersionResolver clientContractVersionResolver,
                                                                   Service service,
                                                                   InboundChannelServiceDefinition definition,
                                                                   InboundRouteActionConfig actionConfig,
                                                                   Set<String> usedRouteIds,
                                                                   String routeIdentityCode) {
        GatewayChannel gatewayChannel = context.gatewayChannel();
        String serviceCode = StringUtils.defaultIfBlank(
                routeIdentityCode,
                service.getCode()
        ).trim();
        String serviceVersion = clientContractVersionResolver.resolve(definition);
        URIBuilder uri = createDefaultUri(gatewayChannel, serviceCode, serviceVersion);
        if (definition != null) {
            if (definition.getMethod() != null) {
                uri.setScheme("rest:" + definition.getMethod().getValue().toLowerCase());
            }
            applyRestPath(uri, gatewayChannel.getPath(), serviceVersion, definition.getPath());
        }

        RouteDefinition routeDefinition = context.routeBuilder().from(uri.toString())
                .routeId(uniqueRouteId(context, serviceCode, serviceVersion, definition, usedRouteIds));
        setEarlyGatewayProperties(routeDefinition, context, definition, service, serviceVersion);
        inboundRouteActionBinder.bind(routeDefinition, actionConfig);
        inboundPathVariablesBinder.bind(routeDefinition, definition);
        ProcessorDefinition<?> pipeline = routeDefinition;
        if (idempotencyProperties.isEnabled()) {
            pipeline = routeDefinition.process(this::reserveIdempotentRequest);
        }

        return Collections.singletonList(new InboundRouteDefinition(routeDefinition, pipeline, definition, serviceVersion));
    }

    private void setEarlyGatewayProperties(RouteDefinition routeDefinition,
                                           GatewayInboundRouteContext context,
                                           InboundChannelServiceDefinition definition,
                                           Service service,
                                           String serviceVersion) {
        RuntimeRoutePlan routePlan = context.routePlan();
        RuntimeServicePlan servicePlan = context.servicePlan();
        GatewayChannel gatewayChannel = context.gatewayChannel();

        routeDefinition.setProperty(Message.RUNTIME_ROUTE_PLAN, constant(routePlan));
        routeDefinition.setProperty(Message.RUNTIME_SERVICE_PLAN, constant(servicePlan));
        routeDefinition.setProperty(Message.SERVICE, constant(service));
        routeDefinition.setProperty(Message.GATEWAY_CHANNEL, constant(gatewayChannel));
        routeDefinition.setProperty(Message.GATEWAY_NAME, constant(gatewayChannel.getName()));
        routeDefinition.setProperty(Message.GATEWAY_CHANNEL_PROTOCOL, constant(gatewayChannel.getProtocolType()));
        routeDefinition.setProperty(Message.CHANNEL_SERVICE_ACCESS, constant(fallbackAccess(routePlan, servicePlan, definition)));
        routeDefinition.setProperty(Message.SERVICE_VERSION, constant(serviceVersion));
        routeDefinition.setProperty(Message.CHANNEL_SERVICE_DEFINITION, constant(definition));
    }

    private List<InboundRouteDefinition> createTaskWorkflowCategoryRoutes(
            GatewayInboundRouteContext context,
            ClientContractVersionResolver versionResolver
    ) {
        List<TaskWorkflowCategoryDefinition> categories =
                taskWorkflowCategoryDefinitions(context, versionResolver);
        if (categories.isEmpty()) {
            throw new IllegalStateException("Gateway " + context.gatewayChannel().getName()
                    + " has active TASK_WORKFLOW services but no shared REST inbound category");
        }
        List<TaskWorkflowCategoryDefinition> owned = categories.stream()
                .filter(category -> category.servicePlan() == context.servicePlan())
                .toList();
        if (owned.isEmpty()) {
            return List.of();
        }
        List<InboundChannelServiceDefinition> definitions = owned.stream()
                .map(TaskWorkflowCategoryDefinition::definition)
                .toList();
        inboundRouteDefinitionValidator.validate(context, definitions);
        Set<String> usedRouteIds = new HashSet<>();
        List<InboundRouteDefinition> routes = new ArrayList<>();
        for (TaskWorkflowCategoryDefinition category : owned) {
            validateTaskWorkflowCategory(category);
            routes.addAll(createRestRouteDefinition(
                    context,
                    versionResolver,
                    context.servicePlan().service(),
                    category.definition(),
                    InboundRouteActionConfig.absent(),
                    usedRouteIds,
                    "task-workflow"
            ));
        }
        return List.copyOf(routes);
    }

    private List<TaskWorkflowCategoryDefinition> taskWorkflowCategoryDefinitions(
            GatewayInboundRouteContext context,
            ClientContractVersionResolver versionResolver
    ) {
        Map<String, TaskWorkflowCategoryDefinition> unique = new LinkedHashMap<>();
        for (RuntimeServicePlan plan : context.routePlan().servicePlans()) {
            if (plan == null || plan.service() == null
                    || plan.service().getRoutingStrategy() != RoutingStrategy.TASK_WORKFLOW
                    || plan.routeDefinitions() == null) {
                continue;
            }
            for (ChannelServiceDefinition routeDefinition : plan.routeDefinitions()) {
                if (routeDefinition == null
                        || routeDefinition.getType() != ChannelServiceDefinitionType.INBOUND) {
                    continue;
                }
                if (!(routeDefinition instanceof InboundChannelServiceDefinition inbound)) {
                    throw new IllegalStateException("TASK_WORKFLOW INBOUND definition "
                            + routeDefinition.getId()
                            + " is not an InboundChannelServiceDefinition");
                }
                String version = versionResolver.resolve(inbound);
                String key = taskWorkflowCategoryKey(inbound, version);
                TaskWorkflowCategoryDefinition category =
                        new TaskWorkflowCategoryDefinition(plan, inbound, version);
                TaskWorkflowCategoryDefinition previous = unique.putIfAbsent(key, category);
                if (previous != null) {
                    throw new IllegalStateException(
                            "Duplicate shared TASK_WORKFLOW REST category method/path/version="
                                    + key + " on serviceCode="
                                    + previous.servicePlan().service().getCode()
                                    + " and serviceCode=" + plan.service().getCode());
                }
            }
        }
        return List.copyOf(unique.values());
    }

    private String taskWorkflowCategoryKey(
            InboundChannelServiceDefinition definition,
            String version
    ) {
        String method = definition.getMethod() == null
                ? "POST"
                : definition.getMethod().name();
        return method + ":" + normalizePath(definition.getPath()).toLowerCase(Locale.ROOT)
                + ":" + version.toLowerCase(Locale.ROOT);
    }

    private void validateTaskWorkflowCategory(
            TaskWorkflowCategoryDefinition category
    ) {
        InboundChannelServiceDefinition definition = category.definition();
        String path = normalizePath(definition.getPath());
        if (!path.toLowerCase(Locale.ROOT).contains("task-workflow")
                || !path.contains("{serviceCode}")
                || !path.contains("{inboundAction}")) {
            throw new IllegalStateException(
                    "Shared TASK_WORKFLOW REST definition " + definition.getId()
                            + " must expose a path equivalent to "
                            + "/task-workflow/{serviceCode}/{inboundAction}");
        }
        if (definition.getDefinition() == null
                || StringUtils.isBlank(definition.getDefinition().getDetails())) {
            return;
        }
        try {
            JsonNode details = objectMapper.readTree(
                    definition.getDefinition().getDetails());
            for (String forbidden : List.of(
                    "inboundAction",
                    "actionPlan",
                    "routingStrategy",
                    "steps")) {
                if (details.has(forbidden)) {
                    throw new IllegalStateException(
                            "Shared TASK_WORKFLOW gateway definition "
                                    + definition.getId()
                                    + " must not own service action-plan field="
                                    + forbidden);
                }
            }
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Shared TASK_WORKFLOW gateway definition "
                            + definition.getId() + " contains invalid JSON",
                    exception);
        }
    }

    void reserveIdempotentRequest(Exchange exchange) {
        String key = StringUtils.trimToNull(
                exchange.getMessage().getHeader(CLIENT_CORRELATION_ID_HEADER, String.class));

        if (key == null) {
            throw new MissingRequiredInputException(CLIENT_CORRELATION_ID_HEADER);
        }

        if (!idempotentRepository.add(key)) {
            log.warn("Duplicated REST request detected. correlationId={}", key);
            throw new DuplicatedRecordFoundException(CLIENT_CORRELATION_ID_HEADER);
        }

        log.debug("Idempotent key added. correlationId={}", key);
    }

    private boolean isIdempotencyEnabled(String routePath) {
        if (!idempotencyProperties.isEnabled()) {
            return false;
        }
//        String normalizedRoutePath = normalizePath(routePath);
//        return idempotencyProperties.getApiPaths().stream()
//                .filter(StringUtils::isNotBlank)
//                .map(this::normalizePath)
//                .anyMatch(configuredPath -> StringUtils.equalsIgnoreCase(normalizedRoutePath, configuredPath)
//                        || StringUtils.endsWithIgnoreCase(normalizedRoutePath, "/" + configuredPath));
        return false;
    }

    private String normalizePath(String path) {
        return StringUtils.strip(StringUtils.trimToEmpty(path), "/");
    }

    private ChannelServiceAccess fallbackAccess(RuntimeRoutePlan routePlan,
                                                RuntimeServicePlan servicePlan,
                                                InboundChannelServiceDefinition definition) {
        if (routePlan.targetKind() != RuntimeTargetKind.CHANNEL) {
            return servicePlan.channelServiceAccess();
        }
        if (definition != null && definition.getChannelServiceAccess() != null) {
            return definition.getChannelServiceAccess();
        }
        return servicePlan.channelServiceAccess();
    }

    private String uniqueRouteId(GatewayInboundRouteContext context,
                                 String serviceCode,
                                 String serviceVersion,
                                 InboundChannelServiceDefinition definition,
                                 Set<String> usedRouteIds) {
        GatewayChannel gatewayChannel = context.gatewayChannel();
        RuntimeTargetKind targetKind = context.routePlan().targetKind();
        String routeId = GatewayRouteIdFactory.singleRouteId(
                targetKind,
                gatewayChannel.getName(),
                serviceCode,
                serviceVersion);
        if (usedRouteIds.add(routeId)) {
            return routeId;
        }
        routeId = GatewayRouteIdFactory.inboundRouteId(
                targetKind,
                gatewayChannel.getName(),
                serviceCode,
                serviceVersion,
                definition);
        int sequence = 2;
        String candidate = routeId;
        while (!usedRouteIds.add(candidate)) {
            candidate = routeId + "-" + sequence++;
        }
        return candidate;
    }

    private URIBuilder createDefaultUri(GatewayChannel gatewayChannel, String serviceCode, String versionText) {
        log.debug("Creating REST route URI for service {}", serviceCode);
        return new URIBuilder()
                .setScheme("rest:post")
                .setPath(gatewayChannel.getPath())
                .appendPath(versionText)
                .appendPath(serviceCode);
    }

    private void applyRestPath(URIBuilder uri, String gatewayPath, String serviceVersion, String routePath) {
        if (StringUtils.isBlank(routePath)) {
            return;
        }
        uri.setPath(gatewayPath);
        if (!startsWithVersion(routePath)) {
            appendPath(uri, serviceVersion);
        }
        appendPath(uri, routePath);
    }

    private boolean startsWithVersion(String routePath) {
        String normalizedPath = StringUtils.trimToEmpty(routePath).toLowerCase(Locale.ROOT);
        return VERSION_PATH_PATTERN.matcher(normalizedPath).matches();
    }

    private void appendPath(URIBuilder uri, String path) {
        String normalizedPath = StringUtils.trimToNull(path);
        if (normalizedPath != null) {
            normalizedPath = StringUtils.strip(normalizedPath, "/");
        }
        if (StringUtils.isNotBlank(normalizedPath)) {
            uri.appendPath(normalizedPath);
        }
    }

    private record TaskWorkflowCategoryDefinition(
            RuntimeServicePlan servicePlan,
            InboundChannelServiceDefinition definition,
            String serviceVersion
    ) {
    }
}
