package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.utils.RouteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@Slf4j
public class ServiceTargetRouter {

    public void buildTarget(RouteDefinition route, Service service) {
        if (Objects.equals(RoutingStrategy.FIRST, service.getRoutingStrategy())) {
            log.debug("Building FIRST service target routeId={} serviceCode={}",
                    route.getRouteId(), service.getCode());
            ServiceOperation serviceOperation = resolveFirstServiceOperation(service);
            route.setProperty(Message.SERVICE_OPERATION, constant(serviceOperation));
            route.setProperty(Message.OPERATION_NAME, constant(serviceOperation.getOperationName()));
            route.to(resolveOperationUrl(serviceOperation.getOperationName()));
            return;
        }

        if (Objects.equals(RoutingStrategy.MULTI_OPERATION, service.getRoutingStrategy())) {
            log.debug("Building MULTI_OPERATION service target routeId={} serviceCode={}",
                    route.getRouteId(), service.getCode());
            ServiceOperation serviceOperation = resolveMultiOperation(route, service);
            route.setProperty(Message.SERVICE_OPERATION, constant(serviceOperation));
            route.setProperty(Message.OPERATION_NAME, constant(serviceOperation.getOperationName()));
            route.to(resolveOperationUrl(serviceOperation.getOperationName()));
            return;
        }

        if (Objects.equals(RoutingStrategy.FAIL_OVER, service.getRoutingStrategy())) {
            log.debug("Building FAIL_OVER service target routeId={} serviceCode={} operationCount={}",
                    route.getRouteId(), service.getCode(), service.getServiceOperations().size());
            MulticastDefinition multicast = route.multicast()
                    .parallelProcessing(false)
                    .stopOnException("false");
            service.getServiceOperations().forEach(serviceOperation -> {
                String operationName = serviceOperation.getOperationName();
                multicast.to(resolveOperationUrl(operationName)).end();
            });
            return;
        }

        log.warn("Unsupported service routing strategy routeId={} serviceCode={} routingStrategy={}",
                route.getRouteId(), service.getCode(), service.getRoutingStrategy());
        throw new IllegalArgumentException("Unsupported routing strategy: " + service.getRoutingStrategy());
    }

    private ServiceOperation resolveFirstServiceOperation(Service service) {
        List<ServiceOperation> activeOperations = service.getServiceOperations()
                .stream()
                .filter(operation -> Boolean.TRUE.equals(operation.getActive()))
                .toList();

        if (activeOperations.isEmpty()) {
            log.warn("No active operation found for FIRST service routing serviceCode={}", service.getCode());
            throw new IllegalStateException("No active operation found for service " + service.getCode());
        }
        if (activeOperations.size() > 1) {
            log.warn("FIRST service routing has more than one active operation serviceCode={} activeOperationCount={}",
                    service.getCode(), activeOperations.size());
            throw new IllegalStateException("FIRST routing requires exactly one active operation for service " + service.getCode());
        }
        return activeOperations.getFirst();
    }

    private ServiceOperation resolveMultiOperation(RouteDefinition route, Service service) {
        String[] splitRouteName = route.getRouteId().split("-");
        return service.getServiceOperations()
                .stream()
                .filter(o -> RouteUtils.getInstance().generateRouteUniqId(o.getOperationName())
                        .equals(splitRouteName[splitRouteName.length - 1]))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No matching operation found for MULTI_OPERATION service routing routeId={} serviceCode={}",
                            route.getRouteId(), service.getCode());
                    return new IllegalStateException("No route found for " + route.getRouteId());
                });
    }

    private String resolveOperationUrl(String operationName) {
        if (StringUtils.contains(operationName, ':')) {
            return operationName;
        }
        return "direct:" + operationName;
    }
}
