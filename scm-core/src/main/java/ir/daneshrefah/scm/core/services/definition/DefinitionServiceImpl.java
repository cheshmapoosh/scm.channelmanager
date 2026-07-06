package ir.daneshrefah.scm.core.services.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.common.data.repository.definition.DefinitionSpecification;
import ir.daneshrefah.scm.common.dto.definition.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.definition.*;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.core.repository.DefinitionRepository;
import ir.daneshrefah.scm.provider.task.utils.PageableUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DefinitionServiceImpl implements DefinitionService {

    private final DefinitionRepository definitionRepository;
    private final DefinitionMapper definitionMapper;
    private final ObjectMapper objectMapper;
    private static final String PLUGIN_PREFIX = "PLUG_";
    private static final String JAVA_PREFIX = "SVC_";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Definition> getAllDefinitions(DefinitionFilterRequest request) {
        return definitionRepository.findAll().stream().map(definitionMapper::toModel).toList();
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Specification<DefinitionEntity> specification = DefinitionSpecification.toSpecification(request);
        Page<DefinitionEntity> definitionEntities = definitionRepository.findAll(specification, pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), definitionEntities.getTotalElements(), definitionEntities.getContent().stream().map(definitionMapper::toDefinitionResponse).toList());
    }

    @Override
    public DefinitionDetailResponse getAllDefinitionDetailsById(DefinitionDetailFilterRequest request) {
        return definitionRepository.findById(request.getId()).map(definitionMapper::toDefinitionDetailResponse).orElse(null);
    }

    @Override
    public DefinitionResponse createDefinition(DefinitionRequest request) {
        request.setId(null);
        validateDetails(request.getDetails(), request.getType());
        normalizeNameByType(request);
        DefinitionEntity entity = definitionMapper.toEntity(request);
        definitionRepository.save(entity);
        return definitionMapper.toDefinitionResponse(entity);
    }

    @Override
    public DefinitionResponse updateDefinition(DefinitionRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        validateDetails(request.getDetails(), request.getType());
        normalizeNameByType(request);
        DefinitionEntity definitionEntity = definitionRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        definitionEntity.setName(request.getName());
        definitionEntity.setTitle(request.getTitle());
        definitionEntity.setEngine(request.getEngine());
        definitionRepository.save(definitionEntity);
        return definitionMapper.toDefinitionResponse(definitionEntity);
    }

    @Override
    public DefinitionResponse createDefinitionDetail(DefinitionDetailRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        ValidationUtils.checkNull(request.getDetails(), () -> new MissingRequiredInputException("details"));
        DefinitionEntity definitionEntity = definitionRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        Definition definition = definitionMapper.toDefinition(request);
        definitionEntity.setDetails(definition.getDetails());
        definitionRepository.save(definitionEntity);
        return definitionMapper.toDefinitionResponse(definitionEntity);
    }

    @Override
    public DefinitionResponse updateDefinitionDetail(DefinitionDetailRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
        ValidationUtils.checkNull(request.getDetails(), () -> new MissingRequiredInputException("details"));
        DefinitionEntity definitionEntity = definitionRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        Definition definition = definitionMapper.toDefinition(request);
        definitionEntity.setDetails(definition.getDetails());
        definitionRepository.save(definitionEntity);
        return definitionMapper.toDefinitionResponse(definitionEntity);
    }

    @Override
    public DefinitionResponse findByName(String name) {
        ValidationUtils.checkBlankString(name, () -> new MissingRequiredInputException("name"));
        DefinitionEntity definitionEntity = definitionRepository.findByName(name).orElseThrow(() -> new NoMatchRecordFoundException("definition"));
        return definitionMapper.toDefinitionResponse(definitionEntity);
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
                String upperCaseName = request.getName().toUpperCase();
                if (!upperCaseName.startsWith("SVC")) {
                    request.setName(JAVA_PREFIX + upperCaseName);
                }
            }
            default -> {
                // Other definition types are referenced by exact name from service and operation configuration.
            }
        }
    }

    public void validateDetails(JsonNode detailsJsonNode, DefinitionType type) throws RuntimeException {
        if (detailsJsonNode == null || detailsJsonNode.isNull()) {
            return;
        }
        Class<? extends DefinitionDetail> targetClass;
        switch (type) {
            case PLUGIN:
                targetClass = PluginDefinitionDetail.class;
                break;
            case JAVA:
                targetClass = JavaDefinitionDetail.class;
                break;
            case MULTIPLE_ROUTE_CONFIG:
                targetClass = JavaMultiRouteDefinitionDetail.class;
                break;
            default:
                return;
        }
        try {
            ObjectMapper mapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            if (detailsJsonNode.isArray()) {
                for (JsonNode node : detailsJsonNode) {
                    mapper.treeToValue(node, targetClass);
                }
            } else if (detailsJsonNode.isObject()) {
                mapper.convertValue(detailsJsonNode, targetClass);
            } else {
                throw new IllegalArgumentException("details must be object or array");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to validate definition details JSON: " + e.getMessage(), e);
        }
    }
}
