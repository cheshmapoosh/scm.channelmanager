package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.*;
import ir.daneshrefah.scm.common.model.dynamic.rest.ParameterNode;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.entity.service.ProxyServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.core.integration.service.scanner.impl.JavaServiceMetadata;
import ir.daneshrefah.scm.core.integration.service.scanner.spec.ClassContextCache;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.core.services.parameter.ParameterParser;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ProxyService;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

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

    private final ParameterParser parameterParser;
    private final ServiceRelationRepository serviceRelationRepository;
    private final ServiceService serviceService;

    public String generateRequestSchema(Service service) {
        if (service instanceof JavaService javaService) {
            return generateJavaServiceRequestSchema(javaService);
        } else if (service instanceof RestExternalService restService) {
            return generateExternalServiceRequestSchema(restService);
        } else if (service instanceof CompositionService compositionService) {
            return generateCompositionServiceRequestSchema(compositionService);
        } else if (service instanceof ProxyService proxyService) {
            return generateProxyServiceRequestSchema(proxyService);
        }
        return null;
    }

    public String generateResponseSchema(Service service) {
        if (service instanceof JavaService javaService) {
            return generateJavaServiceResponseJsonSchema(javaService);
        } else if (service instanceof RestExternalService restService) {
            return generateExternalServiceResponseSchema(restService);

        } else if (service instanceof CompositionService compositionService) {
            return generateCompositionServiceResponseSchema(compositionService);
        } else if (service instanceof ProxyService proxyService) {
            return generateProxyServiceResponseSchema(proxyService);
        }
        return null;
    }


    private String generateCompositionServiceRequestSchema(CompositionService compositionService) {
        return serviceRelationRepository
                .findAllBySourceServiceId(compositionService.getId())
                .stream()
                .map(ServiceRelationEntity::getTargetService)
                .filter(srv -> srv instanceof RestExternalServiceEntity || srv instanceof ProxyServiceEntity)
                .map(service -> {
                    if (service instanceof RestExternalServiceEntity restService) {
                        return generateExternalServiceRequestSchema(ServiceMapper.INSTANCE.toModel(restService));
                    } else {
                        //PROXY SERVICE
                        return generateProxyServiceRequestSchema(ServiceMapper.INSTANCE.toModel((ProxyServiceEntity) service));
                    }
                })
                .findFirst()
                .orElse(null);
    }

    private String generateProxyServiceRequestSchema(ProxyService proxyService) {
        try {
            ProxyService service = (ProxyService) serviceService.findProxyService(proxyService.getId()).orElse(null);
            assert service != null;
            Service fetchTarget = service.getTargetService();
            if (fetchTarget instanceof RestExternalService restExternalService) {
                return generateExternalServiceRequestSchema(restExternalService);
            }
        } catch (Exception ignore) {
        }
        return "{}";
    }

    private String generateExternalServiceRequestSchema(AbstractExternalService<?> externalService) {
        String requestJsonSchema = externalService.getRequestJsonSchema();
        if (StringUtils.isNotBlank(requestJsonSchema)) {
            return requestJsonSchema;
        }
        try {
            ParameterParser.ServiceParameterCache parametersCache = parameterParser.getParametersCache(externalService);
            ParameterNode requestBodyNode = parametersCache.getRequestBodyNode();
            ParameterNode startNode = requestBodyNode;
            if (requestBodyNode.getValue().getType().equals(ParameterType.OBJECT)) {
                List<ParameterNode> nextNodes = requestBodyNode.getNextNodes();
                startNode = nextNodes.get(0);
            }
            return OBJECT_MAPPER.writeValueAsString(generateJsonSchema(startNode));
        } catch (Exception e) {
            return "{}";
        }
    }

    private String generateProxyServiceResponseSchema(ProxyService proxyService) {
        try {
            ProxyService service = (ProxyService) serviceService.findProxyService(proxyService.getId()).orElse(null);
            assert service != null;
            Service fetchTarget = service.getTargetService();
            if (fetchTarget instanceof RestExternalService restExternalService) {
                return generateExternalServiceResponseSchema(restExternalService);
            }
        } catch (Exception ignore) {
        }
        return "{}";
    }

    private String generateCompositionServiceResponseSchema(CompositionService compositionService) {
        return serviceRelationRepository
                .findAllBySourceServiceId(compositionService.getId())
                .stream()
                .map(ServiceRelationEntity::getTargetService)
                .filter(srv -> srv instanceof RestExternalServiceEntity || srv instanceof ProxyServiceEntity)
                .map(service -> {
                    if (service instanceof RestExternalServiceEntity restService) {
                        return generateExternalServiceResponseSchema(ServiceMapper.INSTANCE.toModel(restService));
                    } else {
                        //PROXY SERVICE
                        return generateProxyServiceResponseSchema(ServiceMapper.INSTANCE.toModel((ProxyServiceEntity) service));
                    }
                })
                .findFirst()
                .orElse(null);

    }

    private String generateExternalServiceResponseSchema(AbstractExternalService<?> externalService) {
        String responseJsonSchema = externalService.getResponseJsonSchema();
        if (StringUtils.isNotBlank(responseJsonSchema)) {
            return responseJsonSchema;
        }
        ParameterParser.ServiceParameterCache parametersCache = parameterParser.getParametersCache(externalService);
        ParameterParser.ResponseCache responseCache = parametersCache.getResponseCache();
        return findSuccessResponse(responseCache)
                .map(responseCache::getResponseBodyNode)
                .map(parameterNode -> {
                    try {
                        ParameterNode startNode = parameterNode;
                        if (parameterNode.getValue().getType().equals(ParameterType.OBJECT)) {
                            List<ParameterNode> nextNodes = parameterNode.getNextNodes();
                            startNode = nextNodes.get(0);
                        }
                        return OBJECT_MAPPER.writeValueAsString(generateJsonSchema(startNode));
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .orElse(null);
    }


    private JsonSchema generateJsonSchema(ParameterNode parameterNode) {
        Parameter value = parameterNode.getValue();
        ParameterType type = value.getType();
        switch (type) {
            case OBJECT:
                ObjectSchema objectSchema = new ObjectSchema();
                Map<String, JsonSchema> properties = new HashMap<>();
                if (parameterNode.getNextNodes() != null) {
                    for (ParameterNode nextNode : parameterNode.getNextNodes()) {
                        String propertyName = nextNode.getName();
                        if (propertyName != null) {
                            properties.put(propertyName, generateJsonSchema(nextNode));
                        }
                    }
                }
                objectSchema.setProperties(properties);
                return objectSchema;

            case ARRAY:
                ArraySchema arraySchema = new ArraySchema();
                if (parameterNode.getNextNodes() != null && !parameterNode.getNextNodes().isEmpty()) {
                    arraySchema.setItemsSchema(generateJsonSchema(parameterNode.getNextNodes().get(0)));
                }
                return arraySchema;

            case BOOLEAN:
                return new BooleanSchema();

            case NUMBER:
                return new NumberSchema();

            case STRING:
            default:
                return new StringSchema();
        }
    }


    private Optional<Response> findSuccessResponse(ParameterParser.ResponseCache responseCache) {
        List<Response> conditions = responseCache.getConditions();
        return conditions
                .stream()
                .filter(response -> Objects.isNull(response.getResponseErrorCodeProperty()))
                .filter(response -> Objects.isNull(response.getResponseErrorMessageProperty()))
                .findFirst();
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

    private String generateJavaServiceRequestSchema(Service service) {
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
