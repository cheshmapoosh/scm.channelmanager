package ir.daneshrefah.scm.core.services.plugin;

import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.dto.plugin.*;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.*;
import ir.daneshrefah.scm.common.service.ScmServiceService;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import ir.daneshrefah.scm.core.mapper.plugin.PluginBindingMapper;
import ir.daneshrefah.scm.core.repository.plugin.PluginBindingRepository;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PluginResolverServiceImpl implements PluginResolverService {

    public static final String BASE_PACKAGE_NAME = "ir.daneshrefah";
    private final PluginBindingRepository pluginBindingRepository;
    private final PluginBindingMapper pluginBindingMapper;
    private final ChannelService channelService;
    private final ScmServiceService scmServiceService;
    private final OperationService operationService;
    private final DefinitionService definitionService;
    private final DefinitionMapper definitionMapper;
    private final List<PluginHandler> pluginHandlers;
    private List<PluginHandlerModel> pluginHandlerModels;

    @PostConstruct
    public void getAllHandler() {
        pluginHandlerModels = pluginHandlers
                .stream()
                .filter(pluginHandler -> pluginHandler.getClass().getName().startsWith(BASE_PACKAGE_NAME))
                .map(pluginHandler -> new PluginHandlerModel(StringUtils.uncapitalize(pluginHandler.getClass().getSimpleName())))
                .toList();
    }

    @Override
    public List<PluginDetail> resolveOrderedPluginDetails(Channel channel) {
        PluginBindingEntity channelPluginBindingEntity = pluginBindingRepository.findByScopeAndScopeIdAndActive(
                PluginScope.CHANNEL,
                String.valueOf(channel.getId()),
                true);
        List<PluginDetail> channelPluginDetails = List.of();
        if (channelPluginBindingEntity != null) {
            PluginBinding channelPluginBinding = pluginBindingMapper.toModel(channelPluginBindingEntity);
            channelPluginDetails = channelPluginBinding.getDetails();
        }
        return channelPluginDetails.stream()
                .filter(PluginDetail::getActive)
                .sorted(Comparator.comparingInt(PluginDetail::getOrder))
                .toList();

    }

    @Override
    public List<PluginDetail> resolveOrderedPluginDetails(List<PluginDetail> channelPluginDetails,
                                                          Service service,
                                                          PluginPhase phase) {
        Map<String, PluginDetail> resolved = new LinkedHashMap<>();

        List<PluginDetail> channelPluginDefinitionsByPhase = resolveActivePluginDetails(channelPluginDetails, phase);

        PluginBindingEntity servicePluginBindingEntity = pluginBindingRepository.findByScopeAndScopeIdAndActive(PluginScope.SERVICE, String.valueOf(service.getId()), true);
        List<PluginDetail> servicePluginDetails = List.of();
        if (servicePluginBindingEntity != null) {
            PluginBinding servicePluginBinding = pluginBindingMapper.toModel(servicePluginBindingEntity);
            servicePluginDetails = resolveActivePluginDetails(servicePluginBinding.getDetails(), phase);
        }

        List<PluginDetail> pluginDetails = Stream.of(
                channelPluginDefinitionsByPhase,
                servicePluginDetails
        ).flatMap(Collection::stream).toList();

        for (PluginDetail pluginDetail : pluginDetails) {
            resolved.put(pluginDetail.getName(), pluginDetail);
        }
        return resolved.values().stream()
                .sorted(Comparator.comparingInt(PluginDetail::getOrder))
                .toList();
    }

    @Override
    public List<PluginDetail> resolveOrderedPluginDetails(Operation operation, PluginPhase phase) {
        if (Objects.isNull(operation)) {
            return Collections.emptyList();
        }
        PluginBindingEntity operationPluginBindingEntity = pluginBindingRepository
                .findByScopeAndScopeIdAndActive(PluginScope.OPERATION, operation.getId(), true);
        List<PluginDetail> operationPluginDetails = List.of();
        if (operationPluginBindingEntity != null) {
            PluginBinding operationPluginBinding = pluginBindingMapper.toModel(operationPluginBindingEntity);
            operationPluginDetails = resolveActivePluginDetails(operationPluginBinding.getDetails(), phase);
        }
        return operationPluginDetails.stream()
                .sorted(Comparator.comparingInt(PluginDetail::getOrder))
                .toList();
    }

    private List<PluginDetail> resolveActivePluginDetails(List<PluginDetail> pluginDetails, PluginPhase phase) {
        if (pluginDetails == null) { return List.of(); }

        return pluginDetails.stream()
                .filter(pluginDetail -> Objects.equals(Boolean.TRUE, pluginDetail.getActive()))
                .filter(p -> p.getPhase() == phase)
                .toList();
    }

    @Override
    public PluginBindingResponse getPluginBindingById(PluginBindingRequest request) {
        PluginBindingEntity pluginBindingEntity = pluginBindingRepository.findById(request.getPluginId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return getPluginBindingResponse(pluginBindingEntity,pluginBindingMapper.toPluginBindingResponse(pluginBindingEntity));
    }

    @Override
    public PluginBindingSearchResponse getPluginBindingsByScopeAndScopeId(PluginBindingRequest request) {
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request"));
        ValidationUtils.checkNull(request.getScope(), () -> new MissingRequiredInputException("scope"));
        ValidationUtils.checkEmptyString(request.getScopeId(), () -> new MissingRequiredInputException("scopeId"));
        PluginBindingEntity entity = pluginBindingRepository.findByScopeAndScopeId(request.getScope(), request.getScopeId());
        return pluginBindingMapper.toPluginBindingCreateResponse(entity);
    }

    @Override
    public List<PluginBindingResponse> getPluginBindingsByDefinitionId(PluginBindingRequest request) {
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request"));
        ValidationUtils.checkNull(request.getDefinitionId(), () -> new MissingRequiredInputException("definitionId"));
        List<PluginBindingEntity> pluginBindingEntities =
                pluginBindingRepository.findByDefinitionId(request.getDefinitionId());

        return pluginBindingEntities.stream()
                .map(entity -> {
                    PluginBindingResponse response = pluginBindingMapper.toPluginBindingResponse(entity);
                    return getPluginBindingResponse(entity, response);
                })
                .toList();
    }

    private PluginBindingResponse getPluginBindingResponse(PluginBindingEntity entity, PluginBindingResponse response) {
        switch (entity.getScope()) {
            case CHANNEL -> response.setName(
                    channelService.findChannelById(Short.parseShort(entity.getScopeId())).getName());
            case SERVICE -> response.setName(
                    scmServiceService.findByServiceId(Short.parseShort(entity.getScopeId())).getName());
            case OPERATION -> response.setName(
                    operationService.findById(entity.getScopeId()).getTitle());
        }
        return response;
    }

    @Override
    public PluginBindingResponse createPluginBinding(PluginBindingCreateRequest request) {
        PluginBindingEntity pluginBindingEntity = pluginBindingMapper.toEntity(request);
        DefinitionResponse definitionResponse = definitionService.getDefinitionById(request.getDefinitionId());
        DefinitionEntity definitionEntity = definitionMapper.toEntity(definitionResponse);
        pluginBindingEntity.setDefinition(definitionEntity);
        validateScope(request.getScope(), request.getScopeId());
        pluginBindingRepository.save(pluginBindingEntity);
        return pluginBindingMapper.toPluginBindingResponse(pluginBindingEntity);
    }

    @Override
    public PluginBindingResponse updatePluginBinding(PluginBindingUpdateRequest request) {
        PluginBindingEntity pluginBindingEntity = pluginBindingRepository.findById(request.getId())
                .orElseThrow(() -> new NoMatchRecordFoundException("id"));
        DefinitionResponse definitionResponse = definitionService.getDefinitionById(request.getDefinitionId());
        DefinitionEntity definitionEntity = definitionMapper.toEntity(definitionResponse);
        pluginBindingEntity.setDefinition(definitionEntity);
        if (!Objects.equals(request.getScopeId(), pluginBindingEntity.getScopeId())
                || !Objects.equals(request.getScope(), pluginBindingEntity.getScope())) {
            validateScope(request.getScope(), request.getScopeId());
            pluginBindingEntity.setScope(request.getScope());
            pluginBindingEntity.setScopeId(request.getScopeId());
        }
        pluginBindingEntity.setActive(request.getActive());
        pluginBindingRepository.save(pluginBindingEntity);
        return getPluginBindingResponse(pluginBindingEntity, pluginBindingMapper.toPluginBindingResponse(pluginBindingEntity));
    }

    private void validateScope(PluginScope scope, String scopeId) {
        switch (scope) {
            case CHANNEL -> channelService.findChannelById(Short.parseShort(scopeId));
            case SERVICE -> scmServiceService.findByServiceId(Short.parseShort(scopeId));
            case OPERATION -> operationService.findById(scopeId);
            default -> throw new IllegalArgumentException("Unsupported scope: " + scope);
        }
    }

    @Override
    public List<PluginHandlerModel> getPluginHandlerModels() {
        return pluginHandlerModels
                .stream()
                .sorted(Comparator.comparing(PluginHandlerModel::getName))
                .toList();
    }

}
