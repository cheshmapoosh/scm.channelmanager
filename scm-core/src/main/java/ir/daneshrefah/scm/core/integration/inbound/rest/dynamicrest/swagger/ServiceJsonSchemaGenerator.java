package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.core.integration.service.scanner.impl.JavaServiceMetadata;
import ir.daneshrefah.scm.core.integration.service.scanner.spec.ClassContextCache;
import ir.daneshrefah.scm.core.mapper.ScmServiceMapper;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractAuditableExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ServiceJsonSchemaGenerator {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        OBJECT_MAPPER.registerModule(javaTimeModule);
    }

    private final ServiceRelationRepository serviceRelationRepository;
    private final ScmServiceMapper scmServiceMapper;

    public String generateRequestSchema(ScmService service) {
        if (service instanceof JavaService javaService) {
            return generateJavaServiceRequestSchema(javaService);
        } else if (service instanceof RestExternalService restService) {
            return generateExternalServiceRequestSchema(restService);
        } else if (service instanceof CompositionService compositionService) {
            return generateCompositionServiceRequestSchema(compositionService);
        }
        return null;
    }

    public String generateResponseSchema(ScmService service) {
        if (service instanceof JavaService javaService) {
            return generateJavaServiceResponseJsonSchema(javaService);
        } else if (service instanceof RestExternalService restService) {
            return generateExternalServiceResponseSchema(restService);

        } else if (service instanceof CompositionService compositionService) {
            return generateCompositionServiceResponseSchema(compositionService);
        }
        return null;
    }


    private String generateCompositionServiceRequestSchema(CompositionService compositionService) {
        return serviceRelationRepository
                .findAllBySourceServiceId(compositionService.getId())
                .stream()
                .map(ServiceRelationEntity::getTargetService)
                .filter(srv -> srv instanceof RestExternalServiceEntity)
                .map(service -> {
                    RestExternalServiceEntity restService = (RestExternalServiceEntity) service;
                    return generateExternalServiceRequestSchema(scmServiceMapper.toModel(restService));
                })
                .findFirst()
                .orElse(null);
    }


    private String generateExternalServiceRequestSchema(AbstractAuditableExternalService<?> externalService) {
        String requestJsonSchema = externalService.getRequestJsonSchema();
        if (StringUtils.isNotBlank(requestJsonSchema)) {
            return requestJsonSchema;
        }
        return "{}";
    }

    private String generateCompositionServiceResponseSchema(CompositionService compositionService) {
        return serviceRelationRepository
                .findAllBySourceServiceId(compositionService.getId())
                .stream()
                .map(ServiceRelationEntity::getTargetService)
                .filter(srv -> srv instanceof RestExternalServiceEntity)
                .map(service -> {
                    RestExternalServiceEntity restService = (RestExternalServiceEntity) service;
                    return generateExternalServiceResponseSchema(scmServiceMapper.toModel(restService));
                })
                .findFirst()
                .orElse(null);

    }

    private String generateExternalServiceResponseSchema(AbstractAuditableExternalService<?> externalService) {
        return null; //TODO
    }


    private String generateJavaServiceResponseJsonSchema(JavaService javaService) {
        try {
            String implPath = javaService.getJavaImplementationClassName();
            String[] split = org.apache.commons.lang3.StringUtils.split(implPath, ".");
            String bean = split[0];
            String methodName = split[1].substring(0, split[1].indexOf("("));
            AbstractJavaService beanInstance = ClassLoader.findBeanOrCreateInstanceOfClass(bean, AbstractJavaService.class);
            Method method = Arrays.stream(ReflectionUtils.getAllDeclaredMethods(beanInstance.getClass()))
                    .filter(m -> m.getName().contains(methodName))
                    .findFirst().orElse(null);
            if (Objects.nonNull(method)) {
                Class<?> returnType = method.getReturnType();
                Class<?> modelClass = Class.forName(returnType.getName());
                JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(OBJECT_MAPPER);
                JsonSchema schema = schemaGen.generateSchema(modelClass);
                return OBJECT_MAPPER.writeValueAsString(schema);
            }
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private String generateJavaServiceRequestSchema(ScmService service) {
        try {
            JavaServiceMetadata javaServiceMetadata = ClassContextCache.getInstance().get(ClassContextCache.Repository.JAVA_SERVICE_METADATA, service.getCode(), JavaServiceMetadata.class).orElseThrow();
            Class<?>[] parameterTypes = javaServiceMetadata.getMethod().getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                Class<?> modelClass = Class.forName(parameterType.getName());
                JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(OBJECT_MAPPER);
                JsonSchema schema = schemaGen.generateSchema(modelClass);
                return OBJECT_MAPPER.writeValueAsString(schema);
            }
        } catch (Exception ignore) {
        }
        return null;
    }


}
