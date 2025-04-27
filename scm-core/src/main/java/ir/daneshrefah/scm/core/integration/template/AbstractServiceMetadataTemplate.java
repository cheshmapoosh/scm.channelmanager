package ir.daneshrefah.scm.core.integration.template;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public abstract class AbstractServiceMetadataTemplate implements ServiceMetadataTemplate {
    private List<Parameter> parameters;
    private ParameterDataProvider parameterDataProvider;

    protected Map<String, Object> createContext(Message message) {
        if (CollectionUtils.isEmpty(parameters)) {
            return Map.of();
        }

        if (parameterDataProvider == null) {
            return parameters.stream().collect(Collectors.toMap(
                    Parameter::getName,
                    Parameter::getDefaultValue));
        }

        return parameters.stream().collect(Collectors.toMap(
                Parameter::getName,
                parameter -> parameterDataProvider.extractParameterValue(message, parameter)));
    }
}
