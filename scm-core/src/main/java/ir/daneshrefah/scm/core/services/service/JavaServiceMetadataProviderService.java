package ir.daneshrefah.scm.core.services.service;

import ir.daneshrefah.scm.common.constant.ServiceCode;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.core.entity.service.JavaServiceEntity;
import ir.daneshrefah.scm.core.integration.service.scanner.impl.JavaServiceMetadata;
import ir.daneshrefah.scm.core.integration.service.scanner.spec.ClassContextCache;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ir.daneshrefah.scm.core.integration.service.scanner.spec.ClassContextCache.Repository.JAVA_SERVICE_METADATA;

@Service
@Slf4j
@RequiredArgsConstructor
public class JavaServiceMetadataProviderService {

    public static final String SYSTEM_NAME = "SYSTEM";
    private final ServiceRepository serviceRepository;

    public void javaServiceSynchronization(List<ir.daneshrefah.scm.common.model.service.Service> services) {
        Map<String, Object> metaDataMap = ClassContextCache.getInstance().getRepository(JAVA_SERVICE_METADATA);
        //SYNC EXISTS SERVICES
        services
                .stream()
                .filter(srv -> srv.getImplementationType().equals(ServiceImplementationType.JAVA))
                .map((s) -> (JavaService) s)
                .forEach(srv -> {
                    if (ClassContextCache.getInstance().get(JAVA_SERVICE_METADATA, srv.getCode()).isEmpty()) {
                        srv.setImplemented(false);
                        log.warn(">>> attention! service code [{}] exists on database but does not have any match java method", srv.getCode());
                    } else {
                        srv.setImplemented(true);
                        JavaServiceMetadata metadata = ClassContextCache.getInstance().get(JAVA_SERVICE_METADATA, srv.getCode(), JavaServiceMetadata.class).orElseThrow();
                        syncJavaServiceMethod(srv, metadata, services);
                    }
                });
        //CREATE NEW SERVICE
        metaDataMap.values()
                .stream()
                .map(JavaServiceMetadata.class::cast)
                .filter(metaData -> !services.stream().map(ir.daneshrefah.scm.common.model.service.Service::getCode).toList().contains(metaData.getCode().name()))
                .forEach(javaServiceMetadata -> createNewJavaService(services, javaServiceMetadata));
    }

    private void syncJavaServiceMethod(JavaService javaService, JavaServiceMetadata metadata, List<ir.daneshrefah.scm.common.model.service.Service> services) {
        ParentService parentService = provideJavaServiceParent(services, javaService, metadata);
        String title = metadata.getTitle();
        javaService.setTitle(StringUtils.isBlank(title) ? javaService.getTitle() : title);
        String alias = metadata.getAlias();
        javaService.setAlias(StringUtils.isBlank(alias) ? javaService.getAlias() : alias);
        ServiceType type = metadata.getType();
        javaService.setType(Objects.isNull(type) ? javaService.getType() : type);
        Boolean firstAuth = metadata.getCheckAccessFirstAuthentication();
        javaService.setCheckAccessFirstAuthentication(Objects.isNull(firstAuth) ? javaService.getCheckAccessFirstAuthentication() : firstAuth);
        Boolean secondAuth = metadata.getCheckAccessSecondAuthentication();
        javaService.setCheckAccessSecondAuthentication(Objects.isNull(secondAuth) ? javaService.getCheckAccessSecondAuthentication() : secondAuth);
        Boolean accessSrv = metadata.getCheckAccessService();
        javaService.setCheckAccessService(Objects.isNull(accessSrv) ? javaService.getCheckAccessService() : accessSrv);
        Boolean checkAccessAsset = metadata.getCheckAccessAsset();
        javaService.setCheckAccessAsset(Objects.isNull(checkAccessAsset) ? javaService.getCheckAccessAsset() : checkAccessAsset);
        javaService.setJavaImplementationClassName(metadata.getJavaImplementationClassName());
        javaService.setParent(parentService);
        javaService.setNoneEditableProperties(getNonEditablePropertiesList(metadata));
    }

    private void createNewJavaService(List<ir.daneshrefah.scm.common.model.service.Service> services, JavaServiceMetadata metadata) {
        ParentService parentService = provideJavaServiceParent(services, null, metadata);
        JavaService javaService = new JavaService();
        javaService.setTitle(metadata.getTitle());
        javaService.setCode(metadata.getCode().name());
        javaService.setAlias(metadata.getAlias());
        javaService.setType(metadata.getType());
        javaService.setCheckAccessFirstAuthentication(getFalseBooleanIfNull(metadata.getCheckAccessFirstAuthentication()));
        javaService.setCheckAccessSecondAuthentication(getFalseBooleanIfNull(metadata.getCheckAccessSecondAuthentication()));
        javaService.setCheckAccessService(getFalseBooleanIfNull(metadata.getCheckAccessService()));
        javaService.setCheckAccessAsset(getFalseBooleanIfNull(metadata.getCheckAccessAsset()));
        javaService.setImplementationType(ServiceImplementationType.JAVA);
        javaService.setStatus(ServiceStatus.ACTIVE);
        javaService.setVersion(1);
        javaService.setJavaImplementationClassName(metadata.getJavaImplementationClassName());
        javaService.setParent(parentService);
        javaService.setCreator(SYSTEM_NAME);
        javaService.setLastEditor(SYSTEM_NAME);
        JavaServiceEntity saved = serviceRepository.save(ServiceMapper.INSTANCE.toEntity(javaService));
        JavaService model = ServiceMapper.INSTANCE.toModel(saved);
        model.setImplemented(true);
        model.setNoneEditableProperties(getNonEditablePropertiesList(metadata));
        services.add(model);
        log.info(">>> NEW JAVA SERVICE HAS BEEN REGISTERED WITH CODE [{}] ", saved.getCode());
    }

    private List<String> getNonEditablePropertiesList(JavaServiceMetadata metadata) {
        List<String> propertiesList = new ArrayList<>();
        if (Objects.nonNull(metadata.getCode())) {
            propertiesList.add("code");
        }
        if (StringUtils.isNotBlank(metadata.getTitle())) {
            propertiesList.add("title");
        }
        if (StringUtils.isNotBlank(metadata.getAlias())) {
            propertiesList.add("alias");
        }
        if (Objects.nonNull(metadata.getType())) {
            propertiesList.add("type");
        }
        if (Objects.nonNull(metadata.getCheckAccessFirstAuthentication())) {
            propertiesList.add("checkAccessFirstAuthentication");
        }
        if (Objects.nonNull(metadata.getCheckAccessSecondAuthentication())) {
            propertiesList.add("checkAccessSecondAuthentication");
        }
        if (Objects.nonNull(metadata.getCheckAccessService())) {
            propertiesList.add("checkAccessService");
        }
        if (Objects.nonNull(metadata.getCheckAccessAsset())) {
            propertiesList.add("checkAccessAsset");
        }
        if (Objects.nonNull(metadata.getParentCode())) {
            propertiesList.add("parentId");
        }
        return propertiesList;
    }

    private ParentService provideJavaServiceParent(
            List<ir.daneshrefah.scm.common.model.service.Service> services,
            JavaService javaService,
            JavaServiceMetadata metadata) {
        ServiceCode parentCode = metadata.getParentCode();
        if (Objects.nonNull(parentCode)) {
            return services.stream().filter(s -> s.getCode().equals(parentCode.name()))
                    .map(ParentService.class::cast)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(">>> PARENT CODE [{code}] DOES NOT EXISTS ".replace("{code}", metadata.getParentCode().name())));
        } else if (Objects.nonNull(javaService)) {
            return (ParentService) javaService.getParent();
        }
        return null;
    }

    private Boolean getFalseBooleanIfNull(Boolean inputStatus) {
        if (Objects.isNull(inputStatus)) {
            return false;
        }
        return inputStatus;
    }
}
