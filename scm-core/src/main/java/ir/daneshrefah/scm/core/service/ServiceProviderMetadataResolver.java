package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.service.*;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ServiceProviderMetadataResolver {
    public static final String PROVIDER_META_DATA_PREFIX = "metadata";
    private final ParameterDataProvider parameterDataProvider;

    public AbstractExternalServiceProviderMetadata resolve(AbstractExternalServiceProvider serviceProvider) {
        List<Parameter> configParameters = serviceProvider.getConfigParameter();
        if (Objects.isNull(configParameters)) {
            return null;
        }
        ServiceProviderProtocol protocol = serviceProvider.getProtocol();
        AbstractExternalServiceProviderMetadata metadata;
        Map<String, Object> dictionary = createMapDictionary(configParameters);
        if (ServiceProviderProtocol.CUSTOM.equals(protocol)) {
            metadata = new CustomExternalServiceProviderMetadata();
            fillCustomMetadata(dictionary, metadata);
        } else if (ServiceProviderProtocol.REST.equals(protocol)) {
            metadata = new RestExternalServiceProviderMetadata();
            fillRestMetadata(dictionary, metadata);
        } else {
            metadata = new AbstractExternalServiceProviderMetadata();
            fillGeneralMetadata(dictionary, metadata);
        }
        return metadata;
    }

    private void fillAdditionalMetadata(Map<String, Object> dictionary, AbstractExternalServiceProviderMetadata metadata) {
        Set<String> keySet = dictionary.keySet();
        List<String> reservedProperties = Arrays.stream(MetadataProperties.values()).map(MetadataProperties::getName).toList();
        keySet.stream().filter(key -> !reservedProperties.contains(key))
                .forEach(key -> {
                    metadata.addParam(key, dictionary.get(key));
                    dictionary.remove(key);
                });
    }

    private void fillGeneralMetadata(Map<String, Object> dictionary, AbstractExternalServiceProviderMetadata metadata) {
        Set<String> keySet = dictionary.keySet();
        keySet.forEach(key -> {
            if (key.equalsIgnoreCase(MetadataProperties.SO_TIMEOUT.getName())) {
                metadata.setSoTimeout(Integer.parseInt(String.valueOf(dictionary.get(key))));
            } else if (key.equalsIgnoreCase(MetadataProperties.ENDPOINT.getName())) {
                metadata.setEndpoint(String.valueOf(dictionary.get(key)));
            } else if (key.equalsIgnoreCase(MetadataProperties.CONNECT_TIMEOUT.getName())) {
                metadata.setConnectTimeout(Integer.parseInt(String.valueOf(dictionary.get(key))));
            } else if (key.equalsIgnoreCase(MetadataProperties.RESPONSE_TIMEOUT.getName())) {
                metadata.setResponseTimeout(Integer.parseInt(String.valueOf(dictionary.get(key))));
            }
        });
        fillAdditionalMetadata(dictionary, metadata);
    }

    private void fillRestMetadata(Map<String, Object> dictionary, AbstractExternalServiceProviderMetadata metadata) {
        fillGeneralMetadata(dictionary, metadata);
        Set<String> keySet = dictionary.keySet();
        keySet.forEach(key -> {
            if (key.equalsIgnoreCase(MetadataProperties.DEFAULT_REQUEST_CONTENT_TYPE.getName())) {
                metadata.setSoTimeout(Integer.parseInt(String.valueOf(dictionary.get(key))));
            } else if (key.equalsIgnoreCase(MetadataProperties.DEFAULT_HTTP_METHOD.getName())) {
                metadata.setEndpoint(String.valueOf(dictionary.get(key)));
            }
        });
    }

    private void fillCustomMetadata(Map<String, Object> dictionary, AbstractExternalServiceProviderMetadata metadata) {
        fillGeneralMetadata(dictionary, metadata);
        //FOR CUSTOM METADATA
    }

    private Map<String, Object> createMapDictionary(List<Parameter> configParameters) {
        Map<String, Object> map = new HashMap<>();
        configParameters
                .stream()
                .filter(parameter -> parameter.getName().startsWith(PROVIDER_META_DATA_PREFIX))
                .forEach(parameter -> {
                    String key = parameter.getName().replace(PROVIDER_META_DATA_PREFIX + ".", StringUtils.EMPTY);
                    Object value = parameterDataProvider.extractParameterValue(parameter).orElse(null);
                    map.put(key, value);
                });
        return map;
    }

    @Getter
    @RequiredArgsConstructor
    private enum MetadataProperties {
        SO_TIMEOUT("soTimeout"),
        ENDPOINT("endpoint"),
        CONNECT_TIMEOUT("connectTimeout"),
        RESPONSE_TIMEOUT("responseTimeout"),
        DEFAULT_REQUEST_CONTENT_TYPE("defaultRequestContentType"),
        DEFAULT_HTTP_METHOD("defaultHttpMethod");

        private final String name;
    }

}
