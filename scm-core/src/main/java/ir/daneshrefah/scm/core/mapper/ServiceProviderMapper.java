package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.service.ExternalServiceProviderEntity;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
@Mapper
public interface ServiceProviderMapper {
    ServiceProviderMapper INSTANCE = Mappers.getMapper(ServiceProviderMapper.class);

    ExternalServiceProvider toModel(ExternalServiceProviderEntity entity);

    List<ExternalServiceProvider> toModels(Iterable<ExternalServiceProviderEntity> entities);

}
