package ir.daneshrefah.scm.process.model.constant;

import ir.daneshrefah.scm.process.exception.NotFountProcessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ProcessDeploymentEnum {
    TRANSFER_SUBSCRIPTION_PAYMENT("subscription_payment_transfer", "subscription_payment_transfer.bpmn"),
    ALI_PROCESS("ali-process", "ali-ppp.bpmn"),
    Process_test("Process_test", "Process_test.bpmn"),
    process("process", "process.bpmn");

    private final String keyName;
    private final String fileName;

    public static ProcessDeploymentEnum findByKeyName(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            throw new IllegalArgumentException("deployment key name cannot be null or empty.");
        }
        return Stream.of(ProcessDeploymentEnum.values())
                .filter(e -> e.getKeyName().equals(keyName))
                .findFirst()
                .orElseThrow(() -> new NotFountProcessException(keyName, "Process not found with key name %s: ".formatted(keyName)));
    }
}
