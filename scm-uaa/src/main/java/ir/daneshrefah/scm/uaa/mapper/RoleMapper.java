package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.role.Role;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.service.role.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Mapper(unmappedTargetPolicy =IGNORE, componentModel = SPRING)
public interface RoleMapper {

    Role toModel(RoleEntity entity);

    List<Role> toModels(Iterable<RoleEntity> entities);

    RoleEntity toEntity(Role role);

    @Mapping(target = "id", ignore = true)
    RoleEntity roleDtoToRoleEntity(RoleDTO roleDTO);
    @Mapping(target = "id", ignore = true)
    void updateRoleEntityFromDto(RoleDTO dto, @MappingTarget RoleEntity roleEntity);

}
