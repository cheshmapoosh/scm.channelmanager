package ir.daneshrefah.scm.core.integration.service.metrics;

import ir.daneshrefah.scm.common.model.plugin.PluginPhase;

public interface ServicePluginMetrics {
    void recordPluginExecution(String gatewayName,
                               String channelCode,
                               String serviceCode,
                               String operationName,
                               String pluginName,
                               PluginPhase pluginPhase,
                               long durationNanos,
                               boolean success);

    void recordServiceExecution(String gatewayName,
                                String channelCode,
                                String serviceCode,
                                String operationName,
                                long durationNanos,
                                boolean success);
}
