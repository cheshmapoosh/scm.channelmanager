package ir.daneshrefah.scm.task.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ProcessNameEnum {

    PAYMENT_TRANSFER("paymentTransfer"),
    GENERAL("general");

    private final String processName;
    public static ProcessNameEnum findByProcessName(String processName) {
        return Arrays.stream(ProcessNameEnum.values())
                .filter(processNameEnum -> processNameEnum.getProcessName().equals(processName))
                .findFirst().orElse(null);
    }
}
