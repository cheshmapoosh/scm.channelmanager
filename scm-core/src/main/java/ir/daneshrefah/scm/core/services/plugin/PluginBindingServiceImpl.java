package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import ir.daneshrefah.scm.core.mapper.plugin.PluginBindingMapper;
import ir.daneshrefah.scm.core.repository.plugin.PluginBindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PluginBindingServiceImpl implements PluginBindingService {
    private final PluginBindingRepository pluginBindingRepository;
    private final PluginBindingMapper pluginBindingMapper;
    @Override
    public PluginBinding findByOperation(Operation operation) {
        PluginBindingEntity pluginBindingEntity = pluginBindingRepository.findByScopeAndScopeId(PluginScope.OPERATION, operation.getId());
        if (pluginBindingEntity == null) {
            return null;
        }
        return pluginBindingMapper.toDto(pluginBindingEntity);
    }

    @Override
    public PluginBinding findByChannel(Channel channel) {
        PluginBindingEntity pluginBindingEntity = pluginBindingRepository.findByScopeAndScopeId(PluginScope.CHANNEL, String.valueOf(channel.getId()));
        if (pluginBindingEntity == null) {
            return null;
        }
        return pluginBindingMapper.toDto(pluginBindingEntity);

    }
}
