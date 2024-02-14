package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.person.Role;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Mapper
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    Role toModel(RoleEntity entity);

    List<Role> toModels(Iterable<RoleEntity> entities);

    RoleEntity toEntity(Role role);

}
