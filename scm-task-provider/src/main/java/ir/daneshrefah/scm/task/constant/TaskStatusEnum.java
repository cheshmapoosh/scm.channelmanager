package ir.daneshrefah.scm.task.constant;

import java.io.Serializable;
import java.util.Arrays;

public enum TaskStatusEnum implements Serializable {
    PENDING(1),
    WAITING_FOR_CONFIRM(2),
    WAITING_FOR_ACKNOWLEDGE(3),
    COMPLETE(4),
    CANCEL(5),
    CURRENT(6); //Just used for filter

    private final Integer statusCode;

    TaskStatusEnum(int statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public static TaskStatusEnum findByStatusCode(Integer statusCode) {
        return Arrays
                .stream(TaskStatusEnum.values())
                .filter(processStatusEnum -> processStatusEnum.statusCode.equals(statusCode))
                .findFirst()
                .orElse(null);
    }
}