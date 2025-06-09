package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ScopeEntity;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Mapper(unmappedTargetPolicy =IGNORE, componentModel = SPRING)
public interface ScopeMapper {

    Scope toModel(ScopeEntity entity);

    ScopeEntity toEntity(Scope model);

    List<Scope> toModels(Iterable<ScopeEntity> entities);

}
