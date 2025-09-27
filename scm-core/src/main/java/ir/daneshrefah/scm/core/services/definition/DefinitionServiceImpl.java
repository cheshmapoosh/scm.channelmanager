package ir.daneshrefah.scm.core.services.definition;

import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.common.dto.definition.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.core.repository.DefinitionRepository;
import ir.daneshrefah.scm.task.utils.PageableUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DefinitionServiceImpl implements DefinitionService {

    private final DefinitionRepository definitionRepository;
    private final DefinitionMapper definitionMapper;
    private static final String PLUGIN_PREFIX = "PLUG_";

    @Override
    public List<Definition> getAllDefinitions(DefinitionFilterRequest request) {
        List<DefinitionEntity> definitionEntities = definitionRepository.findAll();
        return definitionEntities.stream().map(definitionMapper::toModel).toList();
    }

    @Override
    public DefinitionResponse getDefinitionById(String id) {
        ValidationUtils.checkBlankString(id, () -> new MissingRequiredInputException("definitionID"));
        DefinitionEntity definitionEntity = definitionRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("definition"));
        return definitionMapper.toDefinitionResponse(definitionEntity);
    }

    @Override
    public PagedResponseData<DefinitionResponse> getAllDefinitionsByTypes(DefinitionFilterRequest request) {
        Pageable pageable = PageableUtils.getPageable(request);
        Page<DefinitionEntity> definitionEntities = definitionRepository.findAllByTypeIn(request.getTypes(), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), definitionEntities.getTotalElements(), definitionEntities.getContent().stream().map(definitionMapper::toDefinitionResponse).toList());
    }

    @Override
    public DefinitionDetailResponse getAllDefinitionDetailsById(DefinitionDetailFilterRequest request) {
        return definitionRepository.findById(request.getId())
                .map(definitionMapper::toDefinitionDetailResponse)
                .orElse(null);
    }

    @Override
    public DefinitionResponse createDefinition(DefinitionRequest request) {
        request.setId(null);
        validateRequest(request);
        normalizeNameByType(request);
        DefinitionEntity entity = definitionMapper.toEntity(request);
        definitionRepository.save(entity);
        return definitionMapper.toDefinitionResponse(entity);
    }

    @Override
    public DefinitionResponse updateDefinition(DefinitionRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        validateRequest(request);
        normalizeNameByType(request);
        DefinitionEntity definitionEntity = definitionRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        DefinitionEntity entity = definitionMapper.toEntity(request);
        entity.setVersion(definitionEntity.getVersion());
        definitionRepository.save(entity);
        return definitionMapper.toDefinitionResponse(entity);
    }

    @Override
    public DefinitionResponse createDefinitionDetail(DefinitionDetailRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        ValidationUtils.checkNull(request.getDetail(), () -> new MissingRequiredInputException("details"));
        DefinitionEntity definitionEntity = definitionRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        Definition definition = definitionMapper.toDefinition(request);
        definitionEntity.setDetails(definition.getDetails());
        return definitionMapper.toDefinitionResponse(definitionEntity);
    }

    @Override
    public DefinitionResponse updateDefinitionDetail(DefinitionDetailRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        ValidationUtils.checkNull(request.getDetail(), () -> new MissingRequiredInputException("details"));
        DefinitionEntity definitionEntity = definitionRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        Definition definition = definitionMapper.toDefinition(request);
        definitionEntity.setDetails(definition.getDetails());
        return definitionMapper.toDefinitionResponse(definitionEntity);
    }

    private void validateRequest(DefinitionRequest request) {
        ValidationUtils.checkBlankString(request.getName(), () -> new MissingRequiredInputException("name"));
        ValidationUtils.checkBlankString(request.getTitle(), () -> new MissingRequiredInputException("title"));
        ValidationUtils.checkNull(request.getEngine(), () -> new MissingRequiredInputException("engine"));
    }

    private void normalizeNameByType(DefinitionRequest request) {
        ValidationUtils.checkNull(request.getType(), () -> new MissingRequiredInputException("type"));
        DefinitionType type = request.getType();
        switch (type) {
            case PLUGIN -> {
                String upperCaseName = request.getName().toUpperCase();
                if (!upperCaseName.startsWith("PLUG")) {
                    request.setName(PLUGIN_PREFIX + upperCaseName);
                }
            }
            case JAVA -> {
                // no-op
            }
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
