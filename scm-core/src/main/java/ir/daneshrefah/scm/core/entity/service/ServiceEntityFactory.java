package ir.daneshrefah.scm.core.entity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.ServiceInfoRequest;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceCreateRequest;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-22
 */
@Deprecated
public class ServiceEntityFactory {

    public static ScmServiceEntity createServiceEntity(ServiceInfoRequest request) {
        ScmServiceEntity entity = createEmptyServiceEntity(request.getImplementationType());
        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setAlias(Objects.nonNull(request.getAlias()) ? request.getAlias() : "");
        entity.setVersion(null != request.getVersion() ? request.getVersion() : 1);
        entity.setType(request.getType());
        entity.setStatus(null != request.getStatus() ? request.getStatus() : ServiceStatus.ACTIVE);
//        entity.setParent(request.getParent());
        entity.setImplementationType(request.getImplementationType());
        entity.setIsSystemic(false);
        entity.setCheckAccessFirstAuthentication(null != request.getCheckAccessFirstAuthentication() ? request.getCheckAccessFirstAuthentication() : false);
        entity.setCheckAccessSecondAuthentication(null != request.getCheckAccessSecondAuthentication() ? request.getCheckAccessSecondAuthentication() : false);
        entity.setCheckAccessService(null != request.getCheckAccessService() ? request.getCheckAccessService() : false);
        entity.setCheckAccessAsset(null != request.getCheckAccessAsset() ? request.getCheckAccessAsset() : false);
        entity.setAmountProperty(request.getAmountProperty());
        entity.setAssetProperty(request.getAssetProperty());
        if (entity instanceof CompositionServiceEntity compositionService) {
            compositionService.setCompositionType(request.getCompositionType());
        }
        if (entity instanceof RestExternalServiceEntity restExternalService) {
            restExternalService.setPath(request.getPath());
            restExternalService.setHttpMethod(request.getHttpMethod());
            restExternalService.setRequestBodyType(request.getRequestBodyType());
            restExternalService.setRequestContentType(request.getRequestContentType());
        }
        return entity;
    }

    public static ParentServiceEntity createServiceEntity(ParentServiceCreateRequest request) {
        ParentServiceEntity entity = new ParentServiceEntity();
        entity.setType(ServiceType.PARENT);
        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setVersion(null != request.getVersion() ? request.getVersion() : 1);
        entity.setStatus(request.getStatus());
        entity.setImplementationType(ServiceImplementationType.PARENT);
        entity.setIsSystemic(false);
        entity.setCheckAccessFirstAuthentication(false);
        entity.setCheckAccessSecondAuthentication(false);
        entity.setCheckAccessAsset(false);
        entity.setCheckAccessService(false);
        return entity;
    }

    public static CompositionServiceEntity createServiceEntity(CompositionServiceCreateRequest request) {
        CompositionServiceEntity entity = new CompositionServiceEntity();
        entity.setCompositionType(request.getCompositionType());
        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setAlias(request.getAlias());
        entity.setVersion(null != request.getVersion() ? request.getVersion() : 1);
        entity.setIsSystemic(false);
        entity.setType(request.getType());
        entity.setStatus(request.getStatus());
        entity.setImplementationType(ServiceImplementationType.COMPOSITION);
        entity.setCheckAccessSecondAuthentication(null != request.getCheckAccessSecondAuthentication() ? request.getCheckAccessSecondAuthentication() : false);
        entity.setCheckAccessFirstAuthentication(null != request.getCheckAccessFirstAuthentication() ? request.getCheckAccessFirstAuthentication() : false);
        entity.setCheckAccessAsset(null != request.getCheckAccessAsset() ? request.getCheckAccessAsset() : false);
        entity.setCheckAccessService(null != request.getCheckAccessService() ? request.getCheckAccessService() : false);
        entity.setAmountProperty(request.getAmountProperty());
        entity.setAssetProperty(request.getAssetProperty());
        return entity;
    }

    public static ScmServiceEntity createEmptyServiceEntity(ServiceImplementationType implementationType) {
        return createEmptyServiceEntity(null, implementationType);
    }

    public static ScmServiceEntity createEmptyServiceEntity(String serviceId, ServiceImplementationType implementationType) {
        ScmServiceEntity result = null;
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

    public static JavaServiceEntity createServiceEntity(JavaServiceCreateRequest request) {
        JavaServiceEntity entity = (JavaServiceEntity) createEmptyServiceEntity(ServiceImplementationType.JAVA);
        entity.setTitle(request.getTitle());
        entity.setCode(request.getCode());
        entity.setVersion(null != request.getVersion() ? request.getVersion() : 1);
        entity.setAlias(Objects.nonNull(request.getAlias()) ? request.getAlias() : "");
        entity.setType(request.getType());
        entity.setStatus(null != request.getStatus() ? request.getStatus() : ServiceStatus.ACTIVE);
        entity.setImplementationType(ServiceImplementationType.JAVA);
        entity.setCheckAccessSecondAuthentication(null != request.getCheckAccessSecondAuthentication() ? request.getCheckAccessSecondAuthentication() : false);
        entity.setCheckAccessFirstAuthentication(null != request.getCheckAccessFirstAuthentication() ? request.getCheckAccessFirstAuthentication() : false);
        entity.setCheckAccessService(null != request.getCheckAccessService() ? request.getCheckAccessService() : false);
        entity.setCheckAccessAsset(null != request.getCheckAccessAsset() ? request.getCheckAccessAsset() : false);
        entity.setIsSystemic(false);
        return entity;
    }

}
