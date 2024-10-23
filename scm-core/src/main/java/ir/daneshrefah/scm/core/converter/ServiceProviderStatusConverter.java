package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ServiceProviderStatusConverter implements AttributeConverter<ServiceProviderStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ServiceProviderStatus serviceProviderStatus) {
        return serviceProviderStatus.getValue();
    }

    @Override
    public ServiceProviderStatus convertToEntityAttribute(Integer integer) {
        return ServiceProviderStatus.fromValue(integer);
    }
}
