package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.core.mapper.gateway.ChannelServiceDefinitionMapper;
import ir.daneshrefah.scm.core.repository.DefinitionRepository;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelServiceDefinitionServiceImpl implements ChannelServiceDefinitionService {
    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ChannelServiceDefinitionMapper channelServiceDefinitionMapper;
    private final DefinitionRepository definitionRepository;
    private final DefinitionMapper definitionMapper;

    @Override
    public List<ChannelServiceDefinition> findDefinitions(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel) {
        return channelServiceDefinitionRepository.findByChannelServiceAccess_IdAndGatewayChannel_Id(
                        channelServiceAccess.getId(),
                        gatewayChannel.getId())
                .stream()
                .map(channelServiceDefinitionMapper::toModel)
                .peek(model -> {
                    if (model instanceof RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
                        enrichRestDefinition(restMultipleChannelServiceDefinition);
                    }
                })
                .toList();

    }

    private void enrichRestDefinition(RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
        restMultipleChannelServiceDefinition
                .getMultiRouteDetails()
                .forEach(multiRouteDetail -> {
                    definitionRepository.findById(multiRouteDetail.getDefinitionId()).ifPresent(definition -> {
                        RestChannelServiceDefinition serviceDefinition = new RestChannelServiceDefinition();
                        BeanUtils.copyProperties(restMultipleChannelServiceDefinition, serviceDefinition);
                        serviceDefinition.setDefinition(definitionMapper.toModel(definition));
                        serviceDefinition.setType(ChannelServiceDefinitionType.REST);
                        channelServiceDefinitionMapper.enrichRestChannelServiceDefinition(serviceDefinition);
                        multiRouteDetail.setDefinition(serviceDefinition);
                    });
                });
    }

}
