package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginDefinition;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;

import java.util.List;

public interface PluginResolverService {

    List<PluginDefinition> resolveOrderedPluignDefinitions(Channel channel);

    List<PluginDefinition> resolveOrderedPluignDefinitions(List<PluginDefinition> channelPluginDefinitions, Service service, PluginPhase phase);

    List<PluginDefinition> resolveOrderedPluignDefinitions(Operation operation, PluginPhase phase);
}
