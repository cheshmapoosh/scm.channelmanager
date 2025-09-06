package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.mapper.ChannelMapper;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.data.mapper.EbServiceMapper;
import ir.daneshrefah.scm.common.data.mapper.ServiceMapper;
import ir.daneshrefah.scm.common.data.repository.assets.ChannelServiceAccessRepository;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessCreateRequest;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.service.ScmServiceService;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.common.service.channel.ChannelServiceAccessService;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ChannelServiceAccessServiceImpl implements ChannelServiceAccessService {
    private final ChannelServiceAccessRepository channelServiceAccessRepository;
    private final ServiceOperationRepository serviceOperationRepository;
    private final ChannelServiceAccessMapper channelServiceAccessMapper;
    private final ServiceOperationMapper serviceOperationMapper;
    private final Map<TerminalType, List<ChannelServiceAccess>> CACHE = new ConcurrentHashMap<>();
    private volatile List<ChannelServiceAccess> CACHE_ALL = List.of();
    private final ScmServiceService scmServiceService;
    private final ChannelService channelService;
    private final ChannelMapper channelMapper;
    private final EbServiceMapper ebServiceMapper;

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
                  ChannelServiceAccess channelServiceAccess = channelServiceAccessMapper.toModel(channelServiceAccessEntity);
                  List<ServiceOperationEntity> serviceOperationEntities = serviceOperationRepository
                          .findAllByService_Id(channelServiceAccessEntity.getService().getId());
                  List<ServiceOperation> serviceOperations = serviceOperationEntities.stream()
                          .map(serviceOperationMapper::toModel)
                          .toList();
                  channelServiceAccess.getService().setServiceOperations(serviceOperations);
                  return channelServiceAccess;
                })
                .collect(Collectors.toList());
    }


    public List<ChannelServiceAccess> findAllByTerminalType(TerminalType terminalType) {
        return CACHE.computeIfAbsent(terminalType, key -> channelServiceAccessRepository
                .findByChannelId(terminalType.getLegacyTerminalId())
                .stream()
                .map(channelServiceAccessMapper::toModel)
                .toList());
    }

    public List<ChannelServiceAccess> findAll() {
        if (CACHE_ALL.isEmpty()) {
            synchronized (this) {
                if (CACHE_ALL.isEmpty()) {
                    CACHE_ALL = List.copyOf(channelServiceAccessMapper.toModel(channelServiceAccessRepository.findAll()));
                }
            }
        }
        return CACHE_ALL;
    }

    public List<ChannelServiceAccess> findAllByServiceId(Short serviceId) {
        return findAll().stream()
                .filter(channelServiceAccess -> channelServiceAccess.getService() != null && java.util.Objects.equals(channelServiceAccess.getService().getId(), serviceId))
                .toList();
    }

    public ChannelServiceAccess create(ChannelServiceAccess channelServiceAccess) {
        ValidationUtils.checkNull(channelServiceAccess.getService(), () -> new MissingRequiredInputException("service"));
        ValidationUtils.checkNull(channelServiceAccess.getService().getId(), () -> new MissingRequiredInputException("serviceId"));
        ValidationUtils.checkNull(channelServiceAccess.getChannel(), () -> new MissingRequiredInputException("channel"));
        ValidationUtils.checkNull(channelServiceAccess.getChannel().getId(), () -> new MissingRequiredInputException("channelId"));
        EbService ebService = scmServiceService.findByServiceId(channelServiceAccess.getService().getId());
        Channel channel = channelService.findChannelById(channelServiceAccess.getChannel().getId());
        ChannelServiceAccessEntity channelServiceAccessEntity = channelServiceAccessMapper.toEntity(channelServiceAccess);
        channelServiceAccessEntity.setChannel(channelMapper.toEntity(channel));
        channelServiceAccessEntity.setService(ebServiceMapper.toEntity(ebService));
        ChannelServiceAccessEntity save = channelServiceAccessRepository.save(channelServiceAccessEntity);
        clearCache();
        return channelServiceAccessMapper.toModel(save);
    }

    private void clearCache() {
        CACHE_ALL = List.of();
        CACHE.clear();
    }

    private static boolean support(ChannelServiceAccessEntity entity) {
        ServiceEntity service = entity.getService();
        return entity.getActive() && service.getPublish() && Objects.nonNull(service.getRoutingStrategy());
    }
}
