package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ChannelServiceDefinitionMapper;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceDefinitionServiceImpl implements ChannelServiceDefinitionService {
    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ChannelServiceDefinitionMapper channelServiceDefinitionMapper;

    @Override
    public RestChannelServiceDefinition findRestDefinition(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel) {
        Optional<ChannelServiceDefinitionEntity> channelServiceDefinitionEntityOptional =
                channelServiceDefinitionRepository.findByChannelServiceAccess_IdAndGatewayChannelCodeAndType(
                        channelServiceAccess.getId(),
                        gatewayChannel.getCode(),
                        ChannelServiceDefinitionType.REST
                );

        return channelServiceDefinitionEntityOptional.map(channelServiceDefinitionMapper::toRestTypeDto).orElse(null);
    }
}
