package ir.daneshrefah.scm.core.integration.service.metrics;

import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import org.springframework.stereotype.Component;

@Component
public class NoopServicePluginMetrics implements ServicePluginMetrics {
    @Override
    public void recordPluginExecution(String gatewayName,
                                      String channelCode,
                                      String serviceCode,
                                      String operationName,
                                      String pluginName,
                                      PluginPhase pluginPhase,
                                      long durationNanos,
                                      boolean success) {
    }

    @Override
    public void recordServiceExecution(String gatewayName,
                                       String channelCode,
                                       String serviceCode,
                                       String operationName,
                                       long durationNanos,
                                       boolean success) {
    }
}
