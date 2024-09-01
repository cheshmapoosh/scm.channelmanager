package ir.daneshrefah.scm.common.model.service.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum DatasourceConditionOperation {
    NONE(0),
    EQUAL(1),
    EQUAL_IGNORE_CASE(2),
    NOT_EQUAL(3),
    NOT_EQUAL_IGNORE_CASE(4),
    CONTAINS(5),
    START_WITH(6),
    END_WITH(7),
    GRATER_THAN(8),
    GRATER_THAN_EQUAL(9),
    LESS_THAN(10),
    LESS_THAN_EQUAL(11);
    private final Integer code;

    public static DatasourceConditionOperation findByCode(Integer code) {
        if (Objects.isNull(code)) {
            return NONE;
        }
        return Arrays
                .stream(values())
                .filter(p -> p.getCode().equals(code))
                .findFirst()
                .orElse(NONE);
    }
}
