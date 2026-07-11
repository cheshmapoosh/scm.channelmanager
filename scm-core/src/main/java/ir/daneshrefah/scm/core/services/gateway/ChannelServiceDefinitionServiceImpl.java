package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionCreateRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionResponse;
import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.common.service.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ChannelServiceDefinitionMapper;
import ir.daneshrefah.scm.core.mapper.gateway.GatewayChannelMapper;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChannelServiceDefinitionServiceImpl implements ChannelServiceDefinitionService {
    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ChannelServiceAccessService channelServiceAccessService;
    private final DefinitionService definitionService;
    private final GatewayService GatewayService;
    private final ChannelServiceDefinitionMapper channelServiceDefinitionMapper;
    private final ChannelServiceAccessMapper channelServiceAccessMapper;
    private final GatewayChannelMapper gatewayChannelMapper;
    private final DefinitionMapper definitionMapper;

    @Override
    public List<ChannelServiceDefinition> findDefinitions(ChannelServiceAccess channelServiceAccess,
                                                          GatewayChannel gatewayChannel) {
        return channelServiceDefinitionRepository
                .findByChannelServiceAccess_IdAndGatewayChannel_Id(
                        channelServiceAccess.getId(),
                        gatewayChannel.getId())
                .stream()
                .map(channelServiceDefinitionMapper::toModel)
                .filter(Objects::nonNull)
                .peek(model -> {
                    if (model instanceof RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
                        enrichRestDefinition(restMultipleChannelServiceDefinition);
                    }
                })
                .toList();
    }

    @Override
    public List<ChannelServiceDefinitionResponse> findDefinitionsByChannelServiceAccess(ChannelServiceDefinitionRequest request) {
        ChannelServiceAccess channelServiceAccess = channelServiceAccessService.findById(request.getId());
        return channelServiceDefinitionRepository.findByChannelServiceAccess_Id(
                        channelServiceAccess.getId())
                .stream()
                .map(channelServiceDefinitionMapper::toChannelServiceDefinition)
                .toList();
    }

    public ChannelServiceDefinitionResponse create(ChannelServiceDefinitionCreateRequest request) {
        ValidationUtils.checkEmptyCollection(request.getOperationNames(), () -> {
            throw new MissingRequiredInputException("definitionIds");
        });
        ChannelServiceAccess channelServiceAccess = channelServiceAccessService.findById(request.getChannelServiceAccessId());
        GatewayChannel gatewayChannel = GatewayService.findById(request.getGatewayId());
        GatewayChannelEntity gatewayChannelEntity = gatewayChannelMapper.toEntity(gatewayChannel);
        ChannelServiceDefinitionEntity channelServiceDefinitionEntity = new ChannelServiceDefinitionEntity();
        channelServiceDefinitionEntity.setGatewayChannel(gatewayChannelEntity);
        ChannelServiceAccessEntity channelServiceAccessEntity = channelServiceAccessMapper.toEntity(channelServiceAccess);
        channelServiceDefinitionEntity.setChannelServiceAccess(channelServiceAccessEntity);
        ir.daneshrefah.scm.common.model.gateway.Service service = channelServiceAccess.getService();
        if (service.getRoutingStrategy() == null) {
            throw new IllegalArgumentException("Routing strategy is required for service: " + service.getName() + "");
        }
        DefinitionEntity definitionEntity = null;

        if (!isInbound(request.getType())) {
            throw new IllegalArgumentException("Only INBOUND definitions create gateway route exposure.");
        }
        channelServiceDefinitionEntity.setType(ChannelServiceDefinitionType.INBOUND);
        if (request.getOperationNames().size() > 1) {
            throw new IllegalArgumentException("Multiple routes must be modeled as multiple INBOUND definitions.");
        }

        DefinitionResponse definitionResponse = definitionService.findByName(request.getOperationNames().get(0));
        definitionEntity = definitionMapper.toEntity(definitionResponse);
        channelServiceDefinitionEntity.setDefinition(definitionEntity);
        channelServiceDefinitionRepository.save(channelServiceDefinitionEntity);
        return channelServiceDefinitionMapper.toChannelServiceDefinition(channelServiceDefinitionEntity);
    }

    private boolean isInbound(ChannelServiceDefinitionType type) {
        return type == ChannelServiceDefinitionType.INBOUND;
    }

}
