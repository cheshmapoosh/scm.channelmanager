package ir.daneshrefah.scm.core.services.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionCreateRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionResponse;
import ir.daneshrefah.scm.common.dto.definition.DefinitionRequest;
import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.model.definition.JavaMultiRouteDefinitionDetail;
import ir.daneshrefah.scm.common.model.definition.MultiRouteDetail;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.common.service.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ChannelServiceDefinitionMapper;
import ir.daneshrefah.scm.core.mapper.gateway.GatewayChannelMapper;
import ir.daneshrefah.scm.core.repository.DefinitionRepository;
import ir.daneshrefah.scm.core.repository.gateway.ChannelServiceDefinitionRepository;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelServiceDefinitionServiceImpl implements ChannelServiceDefinitionService {
    private final ChannelServiceDefinitionRepository channelServiceDefinitionRepository;
    private final ChannelServiceAccessService channelServiceAccessService;
    private final DefinitionService definitionService;
    private final GatewayService GatewayService;
    private final DefinitionRepository definitionRepository;
    private final ChannelServiceDefinitionMapper channelServiceDefinitionMapper;
    private final ChannelServiceAccessMapper channelServiceAccessMapper;
    private final GatewayChannelMapper gatewayChannelMapper;
    private final DefinitionMapper definitionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ChannelServiceDefinition> findDefinitions(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel) {
        return channelServiceDefinitionRepository.findByChannelServiceAccess_IdAndGatewayChannel_Id(
                        channelServiceAccess.getId(),
                        gatewayChannel.getId())
                .stream()
                .map(channelServiceDefinitionMapper::toModel)
                .peek(this::enrichDefinition)
                .toList();
    }

    @Override
    public List<ChannelServiceDefinition> findDefinitions(GatewayChannel gatewayChannel) {
        return channelServiceDefinitionRepository.findByGatewayChannel_Id(gatewayChannel.getId())
                .stream()
                .map(channelServiceDefinitionMapper::toModel)
                .peek(this::enrichDefinition)
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

        if (service.getRoutingStrategy().equals(RoutingStrategy.FIRST)) {
            if (!isInboundRoute(request.getType())) {
                throw new IllegalArgumentException("For FIRST type, only INBOUND_ROUTE type is allowed");
            }
            channelServiceDefinitionEntity.setType(ChannelServiceDefinitionType.INBOUND_ROUTE);
            if (request.getOperationNames().size() > 1) {
                throw new IllegalArgumentException("For INBOUND_ROUTE type, only one definitionId is allowed");
            }
            DefinitionResponse definitionResponse = definitionService.findByName(request.getOperationNames().get(0));
            definitionEntity = definitionMapper.toEntity(definitionResponse);
        }
        if (service.getRoutingStrategy().equals(RoutingStrategy.MULTI_OPERATION)) {
            if (!isInboundRouteGroup(request.getType())) {
                throw new IllegalArgumentException("For MULTI_OPERATION type, only INBOUND_ROUTE_GROUP type is allowed");
            }
            channelServiceDefinitionEntity.setType(ChannelServiceDefinitionType.INBOUND_ROUTE_GROUP);
            ValidationUtils.checkEmptyString(request.getContextPath(), () -> new MissingRequiredInputException("contextPath"));
            List<MultiRouteDetail> multiRouteDetails = new ArrayList<>();
            request.getOperationNames().forEach(operationName -> {
                DefinitionResponse definitionResponse = definitionService.findByName(operationName);
                MultiRouteDetail multiRouteDetail = new MultiRouteDetail();
                multiRouteDetail.setDefinitionId(definitionResponse.getId());
                multiRouteDetail.setOperationCode(definitionResponse.getName());
                multiRouteDetails.add(multiRouteDetail);

            });
            JavaMultiRouteDefinitionDetail javaMultiRouteDefinitionDetail = new JavaMultiRouteDefinitionDetail();
            javaMultiRouteDefinitionDetail.setContextPath(request.getContextPath());
            javaMultiRouteDefinitionDetail.setMultiRouteDetails(multiRouteDetails);
            DefinitionRequest definitionRequest = request.getDefinition();
            definitionRequest.setType(DefinitionType.MULTIPLE_ROUTE_CONFIG);
            definitionRequest.setDetails(objectMapper.valueToTree(javaMultiRouteDefinitionDetail));
            DefinitionResponse definitionResponse = definitionService.createDefinition(definitionRequest);
            definitionEntity = definitionMapper.toEntity(definitionResponse);
        }
        channelServiceDefinitionEntity.setDefinition(definitionEntity);
        channelServiceDefinitionRepository.save(channelServiceDefinitionEntity);
        return channelServiceDefinitionMapper.toChannelServiceDefinition(channelServiceDefinitionEntity);
    }

    private void enrichRestDefinition(RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
        restMultipleChannelServiceDefinition
                .getMultiRouteDetails()
                .forEach(multiRouteDetail -> {
                    definitionRepository.findById(multiRouteDetail.getDefinitionId()).ifPresent(definition -> {
                        RestChannelServiceDefinition serviceDefinition = new RestChannelServiceDefinition();
                        BeanUtils.copyProperties(restMultipleChannelServiceDefinition, serviceDefinition);
                        serviceDefinition.setDefinition(definitionMapper.toModel(definition));
                        serviceDefinition.setType(ChannelServiceDefinitionType.INBOUND_ROUTE);
                        channelServiceDefinitionMapper.enrichRestChannelServiceDefinition(serviceDefinition);
                        multiRouteDetail.setDefinition(serviceDefinition);
                    });
                });
    }

    private void enrichDefinition(ChannelServiceDefinition model) {
        if (model instanceof RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
            enrichRestDefinition(restMultipleChannelServiceDefinition);
        }
    }

    private boolean isInboundRoute(ChannelServiceDefinitionType type) {
        return type == ChannelServiceDefinitionType.INBOUND_ROUTE;
    }

    private boolean isInboundRouteGroup(ChannelServiceDefinitionType type) {
        return type == ChannelServiceDefinitionType.INBOUND_ROUTE_GROUP;
    }

}
