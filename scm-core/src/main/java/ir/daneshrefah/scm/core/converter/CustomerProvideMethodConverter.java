package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.plugin.api.model.service.external.CustomerProvideMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
@Converter
public class CustomerProvideMethodConverter implements AttributeConverter<CustomerProvideMethod, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CustomerProvideMethod enumValue) {
        return enumValue.getCode();
    }

    @Override
    public CustomerProvideMethod convertToEntityAttribute(Integer code) {
        return CustomerProvideMethod.findByCode(code);
    }
}