package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-17
 */
@Component
public class ParameterDataProviderImpl extends ParameterDataProvider {

    @PostConstruct
    public void init() {
        setInstance(this);
    }

    @Override
    public Optional<Object> extractParameterValue(Parameter parameter) {
        if (Objects.isNull(parameter) || Objects.isNull(parameter.getDatasource()) ||
                Objects.isNull(parameter.getDatasource().getProperty()) || StringUtils.isBlank(parameter.getDatasource().getValue())) {
            return Optional.empty();
        }
//        TODO dariush
        switch (parameter.getDatasource().getProperty()) {
            case MESSAGE_VARIABLE:
                break;
        }
        return Optional.empty();
    }
}
