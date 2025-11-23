package ir.daneshrefah.scm.core.services;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.mapper.ChannelMapper;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.data.mapper.EbServiceMapper;
import ir.daneshrefah.scm.common.data.repository.assets.ChannelServiceAccessRepository;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessCreateRequest;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessUpdateRequest;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.common.service.ScmServiceService;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.transaction.Transactional;
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
        return channelServiceAccessMapper.toModel(channelServiceAccessRepository.findAll());
    }

    public List<ChannelServiceAccess> findAllByServiceId(Long serviceId) {
        return channelServiceAccessMapper.toModel(channelServiceAccessRepository.findByServiceId(serviceId));
    }

    @Override
    public ChannelServiceAccess findByChannelAndServiceId(Short channelId, Short serviceId) {
        ValidationUtils.checkNull(channelId, () -> new MissingRequiredInputException("channelId"));
        ValidationUtils.checkNull(serviceId, () -> new MissingRequiredInputException("serviceId"));
        ChannelServiceAccessEntity channelServiceAccessEntity = channelServiceAccessRepository.findByChannelIdAndServiceId(channelId, serviceId).orElseThrow(() -> {
            throw new MissingRequiredInputException("No ChannelServiceAccess found for channelId: " + channelId + " and serviceId: " + serviceId);
        });
        return channelServiceAccessMapper.toModel(channelServiceAccessEntity);
    }

    @Override
    public ChannelServiceAccess findById(Long id) {
        ValidationUtils.checkNull(id, () -> new MissingRequiredInputException("id"));
        ChannelServiceAccessEntity channelServiceAccessEntity = channelServiceAccessRepository.findById(id).orElseThrow(() -> new MissingRequiredInputException("id"));
        return channelServiceAccessMapper.toModel(channelServiceAccessEntity);
    }

    public ChannelServiceAccess create(ChannelAccessCreateRequest request) {
        ValidationUtils.checkNull(request.getServiceId(), () -> new MissingRequiredInputException("serviceId"));
        ValidationUtils.checkNull(request.getChannelId(), () -> new MissingRequiredInputException("channelId"));
        EbService ebService = scmServiceService.findByServiceId(request.getServiceId());
        Channel channel = channelService.findChannelById(request.getChannelId());
        ChannelServiceAccessEntity channelServiceAccessEntity = channelServiceAccessMapper.toEntity(request);
        channelServiceAccessEntity.setChannel(channelMapper.toEntity(channel));
        channelServiceAccessEntity.setService(ebServiceMapper.toEntity(ebService));
        ChannelServiceAccessEntity save = channelServiceAccessRepository.save(channelServiceAccessEntity);
        return channelServiceAccessMapper.toModel(save);
    }

    @Override
    @Transactional
    public ChannelServiceAccess update(ChannelAccessUpdateRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        ChannelServiceAccessEntity entity = channelServiceAccessRepository.findById(request.getId()).orElseThrow(() -> new MissingRequiredInputException("id"));
        ChannelServiceAccessEntity channelServiceAccessEntity = channelServiceAccessMapper.toEntity(request);
        channelServiceAccessEntity.setChannel(entity.getChannel());
        channelServiceAccessEntity.setService(entity.getService());
        channelServiceAccessEntity.setFixedValue(request.getFixedValue());
        channelServiceAccessEntity.setRatedValue(request.getRatedValue());
        return channelServiceAccessMapper.toModel(channelServiceAccessRepository.save(channelServiceAccessEntity));
    }

    private static boolean support(ChannelServiceAccessEntity entity) {
        ServiceEntity service = entity.getService();
        return entity.getActive() && service.getPublish() && Objects.nonNull(service.getRoutingStrategy());
    }
}
