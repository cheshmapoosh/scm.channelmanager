package ir.daneshrefah.scm.core.integration.service.guard;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelServiceAccessGuard {
    private final ChannelServiceAccessService channelServiceAccessService;
    private final IncomingChannelCodeResolver incomingChannelCodeResolver;

    public void check(Exchange exchange, RuntimeServicePlan servicePlan) {
        String channelCode = incomingChannelCodeResolver.resolve(exchange)
                .orElseThrow(() -> new AccessDeniedException(
                        "channelServiceAccessGuard",
                        ERROR_CODE_ACCESS_DENIED,
                        "Incoming channel code is required."));
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        if (service == null) {
            service = servicePlan.service();
        }
        if (service == null || service.getId() == null) {
            throw new AccessDeniedException(
                    "channelServiceAccessGuard",
                    ERROR_CODE_ACCESS_DENIED,
                    "Requested service is required for channel access validation.");
        }

        ChannelServiceAccess access = resolveAccess(channelCode, service, servicePlan);
        if (access != null) {
            exchange.setProperty(Message.CHANNEL_SERVICE_ACCESS, access);
            log.debug("ChannelServiceAccessGuard allowed channel={} service={}",
                    access.getChannel().getCode(), access.getService().getCode());
            return;
        }

        String serviceCode = service.getCode() != null ? service.getCode() : String.valueOf(service.getId());
        log.warn("ChannelServiceAccessGuard rejected channel={} service={} routeId={} exchangeId={}",
                channelCode, serviceCode, exchange.getFromRouteId(), exchange.getExchangeId());
        throw new AccessDeniedException(
                "channelServiceAccessGuard",
                ERROR_CODE_ACCESS_DENIED,
                "Channel " + channelCode + " does not have access to service " + serviceCode);
    }

    private ChannelServiceAccess resolveAccess(String channelCode, Service service, RuntimeServicePlan servicePlan) {
        List<ChannelServiceAccess> accesses = channelServiceAccessService.findAllByServiceId(service.getId().longValue());
        if (accesses == null) {
            return null;
        }
        Set<Long> runtimeMembershipIds = runtimeMembershipIds(servicePlan);
        return accesses.stream()
                .filter(access -> access.getChannel() != null)
                .filter(access -> channelCode.equals(access.getChannel().getCode()))
                .filter(access -> runtimeMembershipIds.isEmpty() || runtimeMembershipIds.contains(access.getId()))
                .filter(access -> Boolean.TRUE.equals(access.getActive()))
                .filter(access -> access.getService() != null)
                .filter(access -> Boolean.TRUE.equals(access.getService().getPublish()))
                .findFirst()
                .orElse(null);
    }

    private Set<Long> runtimeMembershipIds(RuntimeServicePlan servicePlan) {
        if (servicePlan == null || servicePlan.channelServiceAccesses() == null) {
            return Set.of();
        }
        Set<Long> ids = new HashSet<>();
        servicePlan.channelServiceAccesses().stream()
                .map(ChannelServiceAccess::getId)
                .filter(id -> id != null)
                .forEach(ids::add);
        return ids;
    }
}
