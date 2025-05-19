package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;

import java.util.List;

public interface PluginResolverService {

    List<PluginDetail> resolveOrderedPluignDefinitions(Channel channel);

    List<PluginDetail> resolveOrderedPluignDefinitions(List<PluginDetail> channelPluginDetails, Service service, PluginPhase phase);

    List<PluginDetail> resolveOrderedPluignDefinitions(Operation operation, PluginPhase phase);
}
