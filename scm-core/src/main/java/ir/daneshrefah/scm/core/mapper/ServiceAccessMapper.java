package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.customer.ServiceAccess;
import ir.daneshrefah.scm.core.entity.person.ServiceAccessEntity;
import ir.daneshrefah.scm.core.entity.service.ScmServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING,uses = {ScmServiceMapper.class})
public interface ServiceAccessMapper {


    @Mapping(source = "service", target = "service", qualifiedByName = "toService")
    @Mapping(source = "terminal", target = "terminal")
    ServiceAccess toModel(ServiceAccessEntity entity);

    List<ServiceAccess> entitiesToModels(Iterable<ServiceAccessEntity> entities);


}
