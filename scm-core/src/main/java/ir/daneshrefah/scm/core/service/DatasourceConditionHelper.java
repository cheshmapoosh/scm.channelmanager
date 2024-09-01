package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.service.parameter.DatasourceConditionOperation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceConditionHelper {

    @Getter
    private static final DatasourceConditionHelper instance = new DatasourceConditionHelper();

    public boolean checkCondition(DatasourceConditionOperation operation, Object condition, Object value) {
        return switch (operation) {
            case EQUAL_IGNORE_CASE -> String.valueOf(condition).equalsIgnoreCase(String.valueOf(value));
            case CONTAINS -> String.valueOf(value).contains(String.valueOf(condition));
            case END_WITH -> StringUtils.endsWith(String.valueOf(value), String.valueOf(condition));
            case LESS_THAN ->
                    Double.parseDouble(String.valueOf(condition)) < Double.parseDouble(String.valueOf(condition));
            case NOT_EQUAL -> !condition.equals(value);
            case START_WITH -> StringUtils.startsWith(String.valueOf(value), String.valueOf(condition));
            case GRATER_THAN ->
                    Double.parseDouble(String.valueOf(condition)) > Double.parseDouble(String.valueOf(condition));
            case LESS_THAN_EQUAL ->
                    Double.parseDouble(String.valueOf(condition)) <= Double.parseDouble(String.valueOf(condition));
            case GRATER_THAN_EQUAL ->
                    Double.parseDouble(String.valueOf(condition)) >= Double.parseDouble(String.valueOf(condition));
            case NOT_EQUAL_IGNORE_CASE ->
                    !StringUtils.equalsIgnoreCase(String.valueOf(condition), String.valueOf(value));
            default -> condition.equals(value);
        };
    }

}
