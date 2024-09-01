package ir.daneshrefah.scm.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasource;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ProxyService;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProxyServiceManager {
    private static final String TARGET_PROXY_SERVICE_CODE_SYMBOL = "$proxy_";
    private final ServiceRelationRepository serviceRelationRepository;

    protected ProxyService initializeProxy(ProxyService proxyService) {
        try {
            List<ServiceRelationEntity> allRelations = serviceRelationRepository.findAllBySourceServiceId(proxyService.getId());
            if (allRelations.size() == 1) {
                ServiceRelationEntity relationEntity = allRelations.get(0);
                Service targetService = ServiceMapper.INSTANCE.toService(relationEntity.getTargetService());
                Service clonedService = (Service) deepCopy(targetService);
                String serviceCode = clonedService.getCode();
                proxyService.setProxyServiceCode(proxyService.getCode() + TARGET_PROXY_SERVICE_CODE_SYMBOL + serviceCode);
                clonedService.setStatus(ServiceStatus.INTERNAL);
                clonedService.setTargetProxyCode(proxyService.getProxyServiceCode());
                proxyService.setTargetService(clonedService);
                clonedService.setProxy(true);
                applyProxyServiceChanges(proxyService);
                return proxyService;
            } else {
                throw new RuntimeException("couldn't found any relation or more than one relation for proxy service has been found");
            }
        } catch (Exception e) {
            throw new RuntimeException(">>> couldn't initialized proxy service  [" + proxyService.getClass() + "]");
        }
    }

    @SneakyThrows
    private Object deepCopy(Object input) {
        ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();
        String serializedInstance = objectMapper.writeValueAsString(input);
        return objectMapper.readValue(serializedInstance, input.getClass());
    }

    private void applyProxyServiceChanges(ProxyService proxyService) {
        Service targetService = proxyService.getTargetService();
        if (targetService instanceof AbstractExternalService<?> externalService) {
            applyProxyParameters(proxyService, externalService);
        }
    }


    private void applyProxyParameters(ProxyService proxyService, AbstractExternalService<?> targetService) {
        List<Parameter> restParameters = combineAllRestParameters(targetService);
        List<Parameter> proxyParameters = proxyService.getParameters();
        proxyParameters.forEach(proxyParameter -> {
            findParameter(restParameters, proxyParameter.getParent().getId())
                    .ifPresent(targetParameter -> {
                        applyParameterChanges(proxyParameter, targetParameter);
                    });
        });
    }

    private void applyParameterChanges(Parameter proxy, Parameter target) {
        DynamicUpdateUtils.applyChangesIfNotNull(proxy.getName(), target::setName);
        DynamicUpdateUtils.applyChangesIfNotNull(proxy.getTag(), target::setTag);
        DynamicUpdateUtils.applyChangesIfNotNull(proxy.getOrder(), target::setOrder);
        DynamicUpdateUtils.applyChangesIfNotNull(proxy.getDefaultValue(), target::setDefaultValue);
        ParameterDatasource datasource = proxy.getDatasource();
        if (Objects.nonNull(datasource)) {
            ParameterDatasource targetDatasource = target.getDatasource();
            DynamicUpdateUtils.applyChangesIfNotNull(datasource.getProperty(), targetDatasource::setProperty);
            DynamicUpdateUtils.applyChangesIfNotNull(datasource.getValue(), targetDatasource::setValue);
            DynamicUpdateUtils.applyChangesIfNotNull(datasource.getLength(), targetDatasource::setLength);
            DynamicUpdateUtils.applyChangesIfNotNull(datasource.getConvertorCode(), targetDatasource::setConvertorCode);
        }
    }

    private List<Parameter> combineAllRestParameters(AbstractExternalService<?> externalService) {
        List<Parameter> parameters = new ArrayList<>(20);
        parameters.addAll(externalService.getRequestHeaders());
        parameters.addAll(externalService.getResponseHeaders());
        parameters.addAll(externalService.getRequestBody());
        if (externalService instanceof RestExternalService restExternalService) {
            parameters.addAll(restExternalService.getRequestQueryStringVariables());
            parameters.addAll(restExternalService.getRequestPathVariables());
        }
        return parameters;
    }

    private Optional<Parameter> findParameter(List<Parameter> parameters, Long targetParameterId) {
        return parameters
                .stream()
                .filter(parameter -> parameter.getId().equals(targetParameterId))
                .findFirst();
    }


}
