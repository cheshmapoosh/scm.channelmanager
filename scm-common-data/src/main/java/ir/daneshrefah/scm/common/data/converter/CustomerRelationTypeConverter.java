package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CustomerRelationTypeConverter implements AttributeConverter<CustomerRelationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CustomerRelationType customerRelationType) {
        return customerRelationType.getCode();
    }

    @Override
    public CustomerRelationType convertToEntityAttribute(Integer code) {
        return CustomerRelationType.findByCode(code);
    }
}
