package ir.daneshrefah.scm.core.integration.service.guard;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.List;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuntimeChannelGuard {
    private final RuntimeChannelProperties properties;
    private final IncomingChannelCodeResolver incomingChannelCodeResolver;

    public void check(Exchange exchange, RuntimeServicePlan servicePlan) {
        if (!properties.isEnabled()) {
            log.debug("RuntimeChannelGuard allowed channel={} reason=disabled",
                    incomingChannelCodeResolver.resolve(exchange).orElse(null));
            return;
        }
        String channelCode = incomingChannelCodeResolver.resolve(exchange)
                .orElseThrow(() -> new AccessDeniedException(
                        "runtimeChannelGuard",
                        ERROR_CODE_ACCESS_DENIED,
                        "Incoming channel code is required."));

        List<String> allowedChannelCodes = properties.getAllowedChannelCodes();
        if (allowedChannelCodes == null || allowedChannelCodes.contains("*") || allowedChannelCodes.contains(channelCode)) {
            log.debug("RuntimeChannelGuard allowed channel={}", channelCode);
            return;
        }

        log.warn("RuntimeChannelGuard rejected channel={} routeId={} exchangeId={}",
                channelCode, exchange.getFromRouteId(), exchange.getExchangeId());
        throw new AccessDeniedException(
                "runtimeChannelGuard",
                ERROR_CODE_ACCESS_DENIED,
                "Runtime instance does not accept channel " + channelCode);
    }
}
