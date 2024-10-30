package ir.daneshrefah.scm.common.constant;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.model.service.CustomExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.model.service.RestExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Getter
@RequiredArgsConstructor
public enum ParameterAutoCompleteDefinition {

    PROVIDER_ABSTRACT_META_DATA
            (AbstractExternalServiceProviderMetadata.class,
                    ParameterTarget.PROVIDER,
                    ParameterActionType.CONFIG,
                    ParameterAutoCompleteProperty.NAME,
                    "metadata",
                    "additionalParams"
            ),
    PROVIDER_REST_META_DATA
            (RestExternalServiceProviderMetadata.class,
                    ParameterTarget.PROVIDER,
                    ParameterActionType.CONFIG,
                    ParameterAutoCompleteProperty.NAME,
                    "metadata",
                    "additionalParams"
            ),
    PROVIDER_CUSTOM_META_DATA
            (CustomExternalServiceProviderMetadata.class,
                    ParameterTarget.PROVIDER,
                    ParameterActionType.CONFIG,
                    ParameterAutoCompleteProperty.NAME,
                    "metadata",
                    "additionalParams"

            );

    private final Class<?> modelClass;
    private final ParameterTarget parameterTarget;
    private final ParameterActionType actionType;
    private final ParameterAutoCompleteProperty property;
    private final String prefix;
    /**
     * Separate with ',' like : name,age
     */
    private final String ignoreProperties;

    public static List<ParameterAutoCompleteDefinition> find(ParameterTarget parameterTarget, ParameterActionType actionType, ParameterAutoCompleteProperty property) {
        List<ParameterAutoCompleteDefinition> result = new ArrayList<>();
        if (Objects.nonNull(parameterTarget) && Objects.nonNull(property)) {
            Arrays.stream(values())
                    .filter(item -> Objects.isNull(actionType) || item.actionType.equals(actionType))
                    .filter(item -> item.parameterTarget.equals(parameterTarget))
                    .filter(item -> item.property.equals(property))
                    .forEach(result::add);
        }
        return result;
    }

    public static ParameterAutoCompleteDefinition fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findAny().orElse(null);
    }

}
