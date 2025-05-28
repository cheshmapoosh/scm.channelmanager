package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.*;
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
        Optional<ChannelServiceDefinitionEntity> channelServiceDefinitionEntityOptional =
                channelServiceDefinitionRepository.findByChannelServiceAccess_IdAndGatewayChannel_Id(
                        channelServiceAccess.getId(),
                        gatewayChannel.getId());

        return channelServiceDefinitionEntityOptional.map(channelServiceDefinitionMapper::toModel).stream().toList();
    }
}
