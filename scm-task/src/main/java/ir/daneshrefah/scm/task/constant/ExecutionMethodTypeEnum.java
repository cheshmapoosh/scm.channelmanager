package ir.daneshrefah.scm.task.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ExecutionMethodTypeEnum {

    START_PROCESS(1),
    APPROVE_PROCESS(2),
    COMPLETE_TASK(3);

    private final Integer code;

    public static ExecutionMethodTypeEnum findByCode(Integer code) {
        return Arrays.stream(ExecutionMethodTypeEnum.values())
                .filter(executionMethodTypeEnum -> executionMethodTypeEnum.getCode().equals(code))
                .findFirst().orElse(null);
    }

}
