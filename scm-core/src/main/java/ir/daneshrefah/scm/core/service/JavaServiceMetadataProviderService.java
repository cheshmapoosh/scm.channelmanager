package ir.daneshrefah.scm.core.service;

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
                .forEach(srv -> {
                    if (ClassContextCache.getInstance().get(JAVA_SERVICE_METADATA, srv.getCode()).isEmpty()) {
                        srv.setStatus(ServiceStatus.INTERNAL);
                        log.warn(">>> attention! service code [{}] exists on database but does not have any match java method", srv.getCode());
                    } else {
                        JavaServiceMetadata metadata = ClassContextCache.getInstance().get(JAVA_SERVICE_METADATA, srv.getCode(), JavaServiceMetadata.class).orElseThrow();
                        JavaService javaService = (JavaService) srv;
                        syncJavaServiceMethod(javaService, metadata, services);
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
        javaService.setCheckAccessService(Objects.isNull(checkAccessAsset) ? javaService.getCheckAccessAsset() : checkAccessAsset);
        javaService.setJavaImplementationClassName(metadata.getJavaImplementationClassName());
        javaService.setParent(parentService);

    }

    private void createNewJavaService(List<ir.daneshrefah.scm.common.model.service.Service> services, JavaServiceMetadata metadata) {
        ParentService parentService = provideJavaServiceParent(services, null, metadata);
        JavaService javaService = new JavaService();
        javaService.setTitle(metadata.getTitle());
        javaService.setCode(metadata.getCode().name());
        javaService.setAlias(metadata.getAlias());
        javaService.setType(metadata.getType());
        javaService.setCheckAccessFirstAuthentication(metadata.getCheckAccessFirstAuthentication());
        javaService.setCheckAccessSecondAuthentication(metadata.getCheckAccessSecondAuthentication());
        javaService.setCheckAccessService(metadata.getCheckAccessService());
        javaService.setCheckAccessAsset(metadata.getCheckAccessAsset());
        javaService.setImplementationType(ServiceImplementationType.JAVA);
        javaService.setStatus(ServiceStatus.ACTIVE);
        javaService.setVersion(1);
        javaService.setJavaImplementationClassName(metadata.getJavaImplementationClassName());
        javaService.setParent(parentService);
        javaService.setCreator(SYSTEM_NAME);
        javaService.setLastEditor(SYSTEM_NAME);
        JavaServiceEntity saved = serviceRepository.save(ServiceMapper.INSTANCE.toEntity(javaService));
        services.add(ServiceMapper.INSTANCE.toModel(saved));
        log.info(">>> NEW JAVA SERVICE HAS BEEN REGISTERED WITH CODE [{}] ", saved.getCode());
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
}
