package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.CustomExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.RestExternalServiceProvider;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.CustomExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.RestExternalServiceProviderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

    RestExternalServiceProvider toModel(RestExternalServiceProviderEntity entity);

    CustomExternalServiceProvider toModel(CustomExternalServiceProviderEntity entity);

    RestExternalServiceProviderEntity toEntity(RestExternalServiceProvider model);

    CustomExternalServiceProviderEntity toEntity(CustomExternalServiceProvider model);

    default List<AbstractExternalServiceProvider> toModels(Iterable<AbstractExternalServiceProviderEntity> entities) {
        if (Objects.isNull(entities)) {
            return null;
        }
        List<AbstractExternalServiceProvider> result = new ArrayList<>();
        for (Iterator<AbstractExternalServiceProviderEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
            AbstractExternalServiceProviderEntity entity = iterator.next();
            result.add(toServiceProvider(entity));
        }
        return result;
    }

    @Named("toServiceProvider")
    default AbstractExternalServiceProvider toServiceProvider(AbstractExternalServiceProviderEntity entity) {
        if (entity instanceof RestExternalServiceProviderEntity) {
            return toModel((RestExternalServiceProviderEntity) entity);
        } else if (entity instanceof CustomExternalServiceProviderEntity) {
            return toModel((CustomExternalServiceProviderEntity) entity);
        }
        return null;
    }

    @Named("toServiceProviderEntity")
    default AbstractExternalServiceProviderEntity toServiceProviderEntity(AbstractExternalServiceProvider model) {
        if (model instanceof RestExternalServiceProvider) {
            return toEntity((RestExternalServiceProvider) model);
        } else if (model instanceof CustomExternalServiceProvider) {
            return toEntity((CustomExternalServiceProvider) model);
        }
        return null;
    }

}
