package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Data
public abstract class AbstractExternalService<T extends AbstractExternalServiceProvider> extends Service {

    private T serviceProvider;
    private ExternalServiceBodyType requestBodyType;
    private List<Response> responseList;

    public Optional<String> getRequestHeaderStaticValue(String parameterName) {
        return getParameterStaticValue(getParameters(ParameterActionType.REQUEST_HEADER), parameterName);
    }

    private Optional<String> getParameterStaticValue(List<Parameter> parameters, String parameterName) {
        if (StringUtils.isBlank(parameterName) || CollectionUtils.isEmpty(parameters)) {
            Optional.empty();
        }
        return parameters.stream()
                .filter(parameter -> ParameterDatasourceProperty.STATIC.equals(parameter.getDatasource().getProperty()))
                .filter(parameter -> StringUtils.equals(parameterName, parameter.getName()))
                .findFirst().map(parameter -> parameter.getDatasource().getValue());
    }

}
