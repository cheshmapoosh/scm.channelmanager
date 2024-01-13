package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.core.entity.person.ServiceAccessEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.model.person.ServiceAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Mapper
public interface ServiceAccessMapper {

    ServiceAccessMapper INSTANCE = Mappers.getMapper(ServiceAccessMapper.class);

    @Mapping(source = "service", target = "service", qualifiedByName = "mapService")
    @Mapping(source = "terminal", target = "terminal", qualifiedByName = "mapTerminal")
    ServiceAccess toModel(ServiceAccessEntity entity);

    List<ServiceAccess> entitiesToModels(Iterable<ServiceAccessEntity> entities);

    @Named("mapService")
    default Service mapService(ServiceEntity entity) {
        return ServiceMapper.INSTANCE.toService(entity);
    }

    @Named("mapTerminal")
    default Terminal mapTerminal(TerminalEntity entity) {
        return TerminalMapper.INSTANCE.toModel(entity);
    }

}
