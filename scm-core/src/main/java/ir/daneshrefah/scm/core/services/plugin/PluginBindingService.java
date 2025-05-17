package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;

import java.util.List;

public interface PluginBindingService {
    PluginBinding findByOperation(Operation operation);

    PluginBinding findByChannel(Channel channel);
}
