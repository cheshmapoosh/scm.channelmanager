package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceAccess;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.core.entity.gateway.ServiceEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceAccessRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ChannelServiceAccessServiceImpl implements ChannelServiceAccessService {
    private final ChannelServiceAccessRepository channelServiceAccessRepository;
    private final ChannelServiceAccessMapper channelServiceAccessMapper;

    @Override
    public List<ChannelServiceAccess> findAllByChannel(Channel channel) {
        Optional<List<ChannelServiceAccessEntity>> channelServiceAccessEntitiesOptional = channelServiceAccessRepository.findAllByChannelId(channel.getId());
        if (channelServiceAccessEntitiesOptional.isEmpty()) {
            return null;
        }
        List<ChannelServiceAccessEntity> channelServiceAccessEntities = channelServiceAccessEntitiesOptional.get();

        return channelServiceAccessEntities.stream()
                .filter(ChannelServiceAccessServiceImpl::support)
                .map(channelServiceAccessMapper::toDto)
                .collect(Collectors.toList());
    }

    private static boolean support(ChannelServiceAccessEntity entity) {
        ServiceEntity service = entity.getService();
        return entity.getActive() && service.getPublish() && Objects.nonNull(service.getRoutingStrategy());
    }
}
