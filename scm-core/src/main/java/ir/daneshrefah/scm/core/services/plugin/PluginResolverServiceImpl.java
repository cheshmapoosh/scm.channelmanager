package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
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
    public List<PluginDetail> resolveOrderedPluginDetails(Channel channel) {
        PluginBindingEntity channelPluginBindingEntity = pluginBindingRepository.findByScopeAndScopeIdAndActive(
                PluginScope.CHANNEL,
                String.valueOf(channel.getId()),
                true);
        List<PluginDetail> channelPluginDetails = List.of();
        if (channelPluginBindingEntity != null) {
            PluginBinding channelPluginBinding = pluginBindingMapper.toModel(channelPluginBindingEntity);
            channelPluginDetails = channelPluginBinding.getDetails();
        }
        return channelPluginDetails.stream()
                .filter(PluginDetail::getActive)
                .sorted(Comparator.comparingInt(PluginDetail::getOrder))
                .toList() ;

    }
        @Override
    public List<PluginDetail> resolveOrderedPluginDetails(List<PluginDetail> channelPluginDetails,
                                                          Service service,
                                                          PluginPhase phase) {
        Map<String, PluginDetail> resolved = new LinkedHashMap<>();

        List<PluginDetail> channelPluginDefinitionsByPhase = resolveActivePluginDetails(channelPluginDetails, phase);

        PluginBindingEntity servicePluginBindingEntity = pluginBindingRepository.findByScopeAndScopeIdAndActive(PluginScope.SERVICE, String.valueOf(service.getId()), true);
        List<PluginDetail> servicePluginDetails = List.of();
        if (servicePluginBindingEntity != null) {
            PluginBinding servicePluginBinding = pluginBindingMapper.toModel(servicePluginBindingEntity);
            servicePluginDetails = resolveActivePluginDetails(servicePluginBinding.getDetails(), phase);
        }

        List<PluginDetail> pluginDetails = Stream.of(
                channelPluginDefinitionsByPhase,
                servicePluginDetails
        ).flatMap(Collection::stream).toList();

        for (PluginDetail pluginDetail : pluginDetails) {
            resolved.put(pluginDetail.getName(), pluginDetail);
        }
        return resolved.values().stream()
                .sorted(Comparator.comparingInt(PluginDetail::getOrder))
                .toList() ;
    }

    @Override
    public List<PluginDetail> resolveOrderedPluginDetails(Operation operation, PluginPhase phase) {
        PluginBindingEntity operationPluginBindingEntity = pluginBindingRepository
                .findByScopeAndScopeIdAndActive(PluginScope.OPERATION, operation.getId(), true);
        List<PluginDetail> operationPluginDetails = List.of();
        if (operationPluginBindingEntity != null) {
            PluginBinding operationPluginBinding = pluginBindingMapper.toModel(operationPluginBindingEntity);
            operationPluginDetails = resolveActivePluginDetails(operationPluginBinding.getDetails(), phase);
        }
        return operationPluginDetails.stream()
                .sorted(Comparator.comparingInt(PluginDetail::getOrder))
                .toList() ;
    }

    private List<PluginDetail> resolveActivePluginDetails(List<PluginDetail> pluginDetails, PluginPhase phase) {
        if (pluginDetails == null) { return List.of(); }

        return pluginDetails.stream()
                .filter(pluginDetail -> Objects.equals(Boolean.TRUE, pluginDetail.getActive()))
                .filter(p -> p.getPhase() == phase)
                .toList();
    }
}
