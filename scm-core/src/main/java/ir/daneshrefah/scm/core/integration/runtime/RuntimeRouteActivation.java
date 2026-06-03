package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuntimeRouteActivation {
    private final ScmRuntimeProperties scmRuntimeProperties;
    private final RuntimeTargetKindResolver runtimeTargetKindResolver;

    public RuntimeMode runtimeMode() {
        return scmRuntimeProperties.runtimeMode();
    }

    public List<RuntimeTargetProperties> runtimeTargets() {
        return scmRuntimeProperties.runtimeTargets();
    }

    public RuntimeTargetKind resolveTargetKind(GatewayChannel gatewayChannel) {
        RuntimeTargetKind targetKind = runtimeTargetKindResolver.resolve(gatewayChannel);
        log.debug("Runtime target kind resolved gatewayName={} targetKind={}",
                gatewayChannel != null ? gatewayChannel.getName() : null,
                targetKind);
        return targetKind;
    }

    public boolean shouldBuildGatewayRoutes(RuntimeMode runtimeMode, RuntimeTargetKind targetKind) {
        boolean enabled = runtimeMode.channelGatewayRoutesEnabled(targetKind);
        log.debug("Runtime route activation decision layer=gateway runtimeMode={} targetKind={} enabled={}",
                runtimeMode, targetKind, enabled);
        return enabled;
    }

    public boolean shouldBuildServiceRoutes(RuntimeMode runtimeMode, RuntimeTargetKind targetKind) {
        boolean enabled = runtimeMode.serviceExecutionRoutesEnabled(targetKind);
        log.debug("Runtime route activation decision layer=service runtimeMode={} targetKind={} enabled={}",
                runtimeMode, targetKind, enabled);
        return enabled;
    }
}
