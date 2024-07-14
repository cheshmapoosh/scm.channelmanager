package ir.daneshrefah.scm.core.entity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.service.ServiceInfoRequest;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-22
 */
public class ServiceEntityFactory {

    public static ServiceEntity createServiceEntity(ServiceInfoRequest request) {
        ServiceEntity entity = createEmptyServiceEntity(request.getImplementationType());
        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setAlias(Objects.nonNull(request.getAlias()) ? request.getAlias() : "");
        entity.setVersion(null != request.getVersion() ? request.getVersion() : 1);
        if (StringUtils.isNotEmpty(request.getMetadata())) {
            try {
                entity.setMetadata(getObjectMapper().readTree(request.getMetadata()));
            } catch (JsonProcessingException e) {
                throw new InvalidInputException("metadata");
            }
        }
        entity.setType(request.getType());
        entity.setStatus(null != request.getStatus() ? request.getStatus() : ServiceStatus.ACTIVE);
//        entity.setParent(request.getParent());
        entity.setImplementationType(request.getImplementationType());
        entity.setIsSystemic(false);
        entity.setRequestJsonSchema(request.getRequestJsonSchema());
        entity.setResponseJsonSchema(request.getResponseJsonSchema());
        entity.setCheckAccessFirstAuthentication(null != request.getCheckAccessFirstAuthentication() ? request.getCheckAccessFirstAuthentication() : false);
        entity.setCheckAccessSecondAuthentication(null != request.getCheckAccessSecondAuthentication() ? request.getCheckAccessSecondAuthentication() : false);
        entity.setCheckAccessService(null != request.getCheckAccessService() ? request.getCheckAccessService() : false);
        entity.setCheckAccessAsset(null != request.getCheckAccessAsset() ? request.getCheckAccessAsset() : false);
        entity.setCustomerProperty(request.getCustomerProperty());
        entity.setAmountProperty(request.getAmountProperty());
        entity.setAssetProperty(request.getAssetProperty());
        if (entity instanceof JavaServiceEntity javaServiceEntity) {
            javaServiceEntity.setJavaImplementationClassName(request.getJavaImplementationClassName());
        }

        if (entity instanceof CompositionServiceEntity compositionService){
            compositionService.setCompositionType(request.getCompositionType());
        }
        return entity;
    }

    public static ServiceEntity createEmptyServiceEntity(ServiceImplementationType implementationType) {
        return createEmptyServiceEntity(null, implementationType);
    }

    public static ServiceEntity createEmptyServiceEntity(String serviceId, ServiceImplementationType implementationType) {
        ServiceEntity result = null;
        switch (implementationType) {
            case CUSTOM_EXTERNAL:
                result = new CustomExternalServiceEntity();
                break;
            case REST_EXTERNAL:
                result = new RestExternalServiceEntity();
                break;
            case JAVA:
                result = new JavaServiceEntity();
                break;
            case COMPOSITION:
                result = new CompositionServiceEntity();
                break;
            case PARENT:
                result = new ParentServiceEntity();
                break;
            case BPMN:
                return null;
        }
        result.setId(serviceId);
        return result;
    }

    private static ObjectMapper getObjectMapper() {
        return ApplicationConfig.getObjectMapperInstance();
    }

}
