package ir.daneshrefah.scm.task.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum OTPReasonEnum {

    PAYMENT_TRANSFER_INTERNAL(1);

    private final Integer code;

    public static OTPReasonEnum findByCode(Integer code) {
        return Arrays.stream(OTPReasonEnum.values())
                .filter(otpReasonEnum -> otpReasonEnum.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
