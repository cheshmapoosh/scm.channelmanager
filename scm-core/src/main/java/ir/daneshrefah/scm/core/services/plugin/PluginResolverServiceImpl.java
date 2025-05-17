package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;
import ir.daneshrefah.scm.common.model.plugin.PluginDefinition;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import ir.daneshrefah.scm.core.mapper.plugin.PluginBindingMapper;
import ir.daneshrefah.scm.core.repository.plugin.PluginBindingRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PluginResolverServiceImpl implements PluginResolverService {
    private final PluginBindingRepository pluginBindingRepository;
    private final PluginBindingMapper pluginBindingMapper;

    @Override
    public List<PluginDefinition> resolveOrderedPluignDefinitions(Channel channel) {
        PluginBindingEntity channelPluginBindingEntity = pluginBindingRepository.findByScopeAndScopeId(PluginScope.CHANNEL, String.valueOf(channel.getId()));
        List<PluginDefinition> channelPluginDefinitions = List.of();
        if (channelPluginBindingEntity != null) {
            PluginBinding channelPluginBinding = pluginBindingMapper.toDto(channelPluginBindingEntity);
            channelPluginDefinitions = channelPluginBinding.getDefinitions();
        }
        return channelPluginDefinitions.stream()
                .sorted(Comparator.comparingInt(PluginDefinition::getOrder))
                .toList() ;

    }
        @Override
    public List<PluginDefinition> resolveOrderedPluignDefinitions(List<PluginDefinition> channelPluginDefinitions,
                                                                  Service service,
                                                                  PluginPhase phase) {
        Map<String, PluginDefinition> resolved = new LinkedHashMap<>();

        List<PluginDefinition> channelPluginDefinitionsByPhase = resolvePluginDefinitions(channelPluginDefinitions, phase);

        PluginBindingEntity servicePluginBindingEntity = pluginBindingRepository.findByScopeAndScopeId(PluginScope.SERVICE, String.valueOf(service.getId()));
        List<PluginDefinition> servicePluginDefinitions = List.of();
        if (servicePluginBindingEntity != null) {
            PluginBinding servicePluginBinding = pluginBindingMapper.toDto(servicePluginBindingEntity);
            servicePluginDefinitions = resolvePluginDefinitions(servicePluginBinding.getDefinitions(), phase);
        }

        List<PluginDefinition> pluginDefinitions = Stream.of(
                channelPluginDefinitionsByPhase,
                servicePluginDefinitions
        ).flatMap(Collection::stream).toList();

        for (PluginDefinition pluginDefinition : pluginDefinitions) {
            resolved.put(pluginDefinition.getName(), pluginDefinition);
        }
        return resolved.values().stream()
                .sorted(Comparator.comparingInt(PluginDefinition::getOrder))
                .toList() ;
    }

    @Override
    public List<PluginDefinition> resolveOrderedPluignDefinitions(Operation operation, PluginPhase phase) {
        PluginBindingEntity operationPluginBindingEntity = pluginBindingRepository.findByScopeAndScopeId(PluginScope.OPERATION, operation.getId());
        List<PluginDefinition> operationPluginDefinitions = List.of();
        if (operationPluginBindingEntity != null) {
            PluginBinding channelPluginBinding = pluginBindingMapper.toDto(operationPluginBindingEntity);
            operationPluginDefinitions = resolvePluginDefinitions(channelPluginBinding.getDefinitions(), phase);
        }
        return operationPluginDefinitions.stream()
                .sorted(Comparator.comparingInt(PluginDefinition::getOrder))
                .toList() ;
    }

    private List<PluginDefinition> resolvePluginDefinitions(List<PluginDefinition> pluginDefinitions, PluginPhase phase) {
        if (pluginDefinitions == null) { return List.of(); }

        return pluginDefinitions.stream()
                .filter(p -> p.getPhase() == phase)
                .toList();
    }
}
