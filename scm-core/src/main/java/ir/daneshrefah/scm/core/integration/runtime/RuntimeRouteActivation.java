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
}
