package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.CustomExternalServiceProvider;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.CustomExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.RestExternalServiceProviderEntity;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public abstract class ServiceProviderMapper {

    @Mapping(target = "metadata", ignore = true)
    public abstract RestExternalServiceProvider toModel(RestExternalServiceProviderEntity entity);

    @Mapping(target = "metadata", ignore = true)
    public abstract CustomExternalServiceProvider toModel(CustomExternalServiceProviderEntity entity);

    public abstract RestExternalServiceProviderEntity toEntity(RestExternalServiceProvider model);

    public abstract CustomExternalServiceProviderEntity toEntity(CustomExternalServiceProvider model);


    public List<AbstractAuditableExternalServiceProvider> toModels(Iterable<AbstractExternalServiceProviderEntity> entities) {
        if (Objects.isNull(entities)) {
            return null;
        }
        List<AbstractAuditableExternalServiceProvider> result = new ArrayList<>();
        for (AbstractExternalServiceProviderEntity entity : entities) {
            result.add(toServiceProvider(entity));
        }
        return result;
    }

    @Named("toServiceProvider")
    protected AbstractAuditableExternalServiceProvider toServiceProvider(AbstractExternalServiceProviderEntity entity) {
        if (entity instanceof RestExternalServiceProviderEntity restExternalServiceProviderEntity) {
            return toModel(restExternalServiceProviderEntity);
        } else if (entity instanceof CustomExternalServiceProviderEntity customExternalServiceProviderEntity) {
            return toModel(customExternalServiceProviderEntity);
        }
        return null;
    }

    @Named("toServiceProviderEntity")
    protected AbstractExternalServiceProviderEntity toServiceProviderEntity(AbstractAuditableExternalServiceProvider model) {
        if (model instanceof RestExternalServiceProvider restExternalServiceProvider) {
            return toEntity(restExternalServiceProvider);
        } else if (model instanceof CustomExternalServiceProvider customExternalServiceProvider) {
            return toEntity(customExternalServiceProvider);
        }
        return null;
    }


}
