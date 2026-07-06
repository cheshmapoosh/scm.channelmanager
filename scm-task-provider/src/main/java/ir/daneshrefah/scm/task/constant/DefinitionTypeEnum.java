package ir.daneshrefah.scm.task.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum DefinitionTypeEnum {

    PROCESS(1),
    TASK(2);

    private final Integer code;

    public static DefinitionTypeEnum findByCode(Integer code) {
       return Arrays.stream(DefinitionTypeEnum.values())
                .filter(definitionTypeEnum -> definitionTypeEnum.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
