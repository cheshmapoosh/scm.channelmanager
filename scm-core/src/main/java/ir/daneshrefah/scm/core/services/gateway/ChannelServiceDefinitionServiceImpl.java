package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ChannelServiceDefinitionMapper;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceDefinitionServiceImpl implements ChannelServiceDefinitionService {
    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ChannelServiceDefinitionMapper channelServiceDefinitionMapper;

    @Override
    public List<ChannelServiceDefinition> findDefinitions(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel) {
        Optional<List<ChannelServiceDefinitionEntity>> channelServiceDefinitionEntitiesOptional =
                channelServiceDefinitionRepository.findByChannelServiceAccess_IdAndGatewayChannel_Id(
                        channelServiceAccess.getId(),
                        gatewayChannel.getId());
        return channelServiceDefinitionEntitiesOptional
                .map(channelServiceDefinitionEntities ->
                        channelServiceDefinitionEntities.stream()
                                .map(channelServiceDefinitionMapper::toModel).toList())
                .orElseGet(List::of);
    }
}
