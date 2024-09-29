package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.OTPReasonEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OTPReasonConverter implements AttributeConverter<OTPReasonEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OTPReasonEnum otpReasonEnum) {
        return otpReasonEnum.getCode();
    }

    @Override
    public OTPReasonEnum convertToEntityAttribute(Integer code) {
        return OTPReasonEnum.findByCode(code);
    }
}
