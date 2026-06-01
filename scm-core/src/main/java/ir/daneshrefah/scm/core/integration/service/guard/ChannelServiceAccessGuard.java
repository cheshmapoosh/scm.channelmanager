package ir.daneshrefah.scm.core.integration.service.guard;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

@Component
@Slf4j
public class ChannelServiceAccessGuard {
    public void check(Exchange exchange, RuntimeServicePlan servicePlan) {
        ChannelServiceAccess access = servicePlan.channelServiceAccess();
        if (access != null
                && Boolean.TRUE.equals(access.getActive())
                && access.getService() != null
                && Boolean.TRUE.equals(access.getService().getPublish())) {
            log.debug("ChannelServiceAccessGuard allowed channel={} service={}",
                    access.getChannel().getCode(), access.getService().getCode());
            return;
        }

        String channelCode = access != null && access.getChannel() != null ? access.getChannel().getCode() : "unknown";
        String serviceCode = access != null && access.getService() != null ? access.getService().getCode() : "unknown";
        log.warn("ChannelServiceAccessGuard rejected channel={} service={} routeId={} exchangeId={}",
                channelCode, serviceCode, exchange.getFromRouteId(), exchange.getExchangeId());
        throw new AccessDeniedException(
                "channelServiceAccessGuard",
                ERROR_CODE_ACCESS_DENIED,
                "Channel " + channelCode + " does not have access to service " + serviceCode);
    }
}
