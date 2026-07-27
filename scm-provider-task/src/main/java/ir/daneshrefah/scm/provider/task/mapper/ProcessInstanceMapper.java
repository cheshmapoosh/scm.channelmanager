package ir.daneshrefah.scm.provider.task.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceApproveResponse;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceResponse;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceStartResponse;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class ProcessInstanceMapper {

    @Autowired
    private ResourceBundleService bundle;
    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "executionId", ignore = true)
    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapCreateAt")
    @Mapping(source = "processStatus", target = "statusName", qualifiedByName = "mapProcessStatusName")
    @Mapping(source = "watcherEntities", target = "attribute", qualifiedByName = "mapToAttribute")
    @Mapping(source = "watcherEntities", target = "transactionData", qualifiedByName = "mapToMetadata")
    public abstract ProcessInstanceResponse toProcessInstanceResponse(ProcessInstanceEntity processInstance);

    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "executionId", ignore = true)
    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapCreateAt")
    @Mapping(source = "processStatus", target = "statusName", qualifiedByName = "mapProcessStatusName")
    public abstract ProcessInstanceApproveResponse toProcessInstanceApproveResponse(ProcessInstanceEntity processInstance);

    @Mapping(target = "executionId", ignore = true)
    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapCreateAt")
    @Mapping(source = "processStatus", target = "statusName", qualifiedByName = "mapProcessStatusName")
    public abstract ProcessInstanceStartResponse toProcessInstanceStartResponse(ProcessInstanceEntity processInstance);

    public abstract ProcessInstanceUpdateResponse toProcessInstanceUpdateResponse(ProcessInstanceEntity processInstance);

    @Named("mapCreateAt")
    String mapCreateAt(Date createAt) {
        return createAt != null ? String.valueOf(createAt.getTime()) : null;
    }

    @Named("mapProcessStatusName")
    String mapStatusName(ProcessStatusEnum processStatusEnum) {
        return bundle.get(AccessibleLocale.FA_IR.getLocale(), processStatusEnum.name()).orElse(processStatusEnum.name());
    }

    @Named("mapToMetadata")
    public JsonNode mapToMetadata(List<ProcessInstanceWatcherEntity> entities) {
        ObjectNode result = objectMapper.createObjectNode();
        entities.stream()
                .filter(e -> e.getType().equals(ProcessWatcherEnum.REQUEST))
                .map(ProcessInstanceWatcherEntity::getData)
                .forEach(json -> result.setAll((ObjectNode) json));
        return result;
    }

    @Named("mapToAttribute")
    public JsonNode mapToAttribute(List<ProcessInstanceWatcherEntity> entities) {
        ObjectNode result = objectMapper.createObjectNode();
        entities.stream()
                .filter(e -> e.getType() != null && e.getType().equals(ProcessWatcherEnum.ATTRIBUTE))
                .map(ProcessInstanceWatcherEntity::getData)
                .forEach(json ->
                        {
                            if (json.has("value")) {
                                result.setAll((ObjectNode) json);
                            }
                        }
                );  // append fields
        return result;
    }
}
