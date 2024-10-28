package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OTPReasonConverter implements AttributeConverter<OtpReason, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OtpReason otpReason) {
        return otpReason.getCode();
    }

    @Override
    public OtpReason convertToEntityAttribute(Integer code) {
        return OtpReason.findByCode(code);
    }
}
