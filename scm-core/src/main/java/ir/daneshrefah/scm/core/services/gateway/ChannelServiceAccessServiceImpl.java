package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.data.repository.assets.ChannelServiceAccessRepository;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ChannelServiceAccessServiceImpl implements ChannelServiceAccessService {
    private final ChannelServiceAccessRepository channelServiceAccessRepository;
    private final ServiceOperationRepository serviceOperationRepository;
    private final ChannelServiceAccessMapper channelServiceAccessMapper;
    private final ServiceOperationMapper serviceOperationMapper;

    @Override
    public List<ChannelServiceAccess> findAllByChannel(Channel channel) {
        Optional<List<ChannelServiceAccessEntity>> channelServiceAccessEntitiesOptional = channelServiceAccessRepository.findAllByChannelId(channel.getId());
        if (channelServiceAccessEntitiesOptional.isEmpty()) {
            return null;
        }
        List<ChannelServiceAccessEntity> channelServiceAccessEntities = channelServiceAccessEntitiesOptional.get();

        return channelServiceAccessEntities.stream()
                .filter(ChannelServiceAccessServiceImpl::support)
                .map(channelServiceAccessEntity -> {
                  ChannelServiceAccess channelServiceAccess = channelServiceAccessMapper.toDto(channelServiceAccessEntity);
                  List<ServiceOperationEntity> serviceOperationEntities = serviceOperationRepository
                          .findAllByService_Id(channelServiceAccessEntity.getService().getId());
                  List<ServiceOperation> serviceOperations = serviceOperationEntities.stream()
                          .map(serviceOperationMapper::toDto)
                          .toList();
                  channelServiceAccess.getService().setServiceOperations(serviceOperations);
                  return channelServiceAccess;
                })
                .collect(Collectors.toList());
    }

    private static boolean support(ChannelServiceAccessEntity entity) {
        ServiceEntity service = entity.getService();
        return entity.getActive() && service.getPublish() && Objects.nonNull(service.getRoutingStrategy());
    }
}
