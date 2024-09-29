package ir.daneshrefah.scm.task.constant;

import java.io.Serializable;
import java.util.Arrays;

public enum ProcessStatusEnum implements Serializable {

    START(0),
    PENDING(1),
    WAITING_FOR_CONFIRM(2),
    WAITING_FOR_ACKNOWLEDGE(3),
    COMPLETE(4),
    CANCEL(5),
    FAIL(6);

    private final Integer statusCode;

    ProcessStatusEnum(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static ProcessStatusEnum findByStatusCode(Integer statusCode) {
        return Arrays
                .stream(ProcessStatusEnum.values())
                .filter(processStatusEnum -> processStatusEnum.statusCode.equals(statusCode))
                .findFirst()
                .orElse(null);
    }
}