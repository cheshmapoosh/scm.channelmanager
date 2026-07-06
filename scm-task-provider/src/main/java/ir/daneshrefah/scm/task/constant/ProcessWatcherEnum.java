package ir.daneshrefah.scm.task.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ProcessWatcherEnum {
    EMPTY(0),
    REQUEST(1),
    ATTRIBUTE(2);

    private Integer code;

    public static ProcessWatcherEnum findByCode(Integer code) {
        return Arrays
                .stream(ProcessWatcherEnum.values())
                .filter(processWatcher -> processWatcher.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
