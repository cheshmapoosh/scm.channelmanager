package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.Service;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Shares one startup-compiled action plan across gateway, service and operation route construction.
 */
@Component
@RequiredArgsConstructor
public class ActionDispatchPlanCatalog {
    private final ActionDispatchRoutePlanFactory routePlanFactory;
    private final ConcurrentMap<String, ActionDispatchPlan> plans = new ConcurrentHashMap<>();

    public ActionDispatchPlan planFor(Service service) {
        String key = serviceKey(service);
        return plans.computeIfAbsent(key, ignored -> routePlanFactory.create(service));
    }

    private String serviceKey(Service service) {
        if (service == null) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service must not be null.");
        }
        if (service.getId() != null) {
            return "id:" + service.getId();
        }
        String code = StringUtils.trimToNull(service.getCode());
        if (code == null) {
            throw ServiceActionException.configurationInvalid(
                    "ACTION_DISPATCH service code must not be blank.");
        }
        return "code:" + code.toLowerCase(Locale.ROOT);
    }
}
